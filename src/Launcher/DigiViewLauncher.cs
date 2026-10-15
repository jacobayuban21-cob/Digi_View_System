using System;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Forms;
using Microsoft.Win32;

namespace DigiViewLauncher
{
    /// <summary>
    /// Native Windows Launcher for DigiView LMS.
    /// Universal JVM Compatibility Engine:
    /// - Intelligently locates any installed Java Runtime (Java 8, 11, 17, 21, 25+)
    /// - Searches bundled JRE, JAVA_HOME, JRE_HOME, Windows Registry, and standard Program Files vendor directories
    /// - Dual-mode execution:
    ///   1. Silent GUI Mode (Explorer double-click): Launches cleanly without opening a command prompt.
    ///   2. Active Log-Listening Console Mode (DigiView-Debug.bat or --debug):
    ///      Attaches to the calling terminal, streams real-time stdout/stderr, writes to digiview-runtime.log,
    ///      and retains the console until application exit.
    /// </summary>
    static class Program
    {
        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern bool AttachConsole(int dwProcessId);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern bool AllocConsole();

        private const int ATTACH_PARENT_PROCESS = -1;

        [STAThread]
        static int Main(string[] args)
        {
            bool isDebug = false;
            foreach (string arg in args)
            {
                if (arg.Equals("--debug", StringComparison.OrdinalIgnoreCase) ||
                    arg.Equals("-d", StringComparison.OrdinalIgnoreCase) ||
                    arg.Equals("--console", StringComparison.OrdinalIgnoreCase) ||
                    arg.Equals("--log", StringComparison.OrdinalIgnoreCase) ||
                    arg.Equals("/debug", StringComparison.OrdinalIgnoreCase) ||
                    arg.Equals("/console", StringComparison.OrdinalIgnoreCase))
                {
                    isDebug = true;
                    break;
                }
            }

            // Try to attach to the calling terminal session (e.g., DigiView-Debug.bat or cmd)
            bool hasConsole = AttachConsole(ATTACH_PARENT_PROCESS);

            if (isDebug && !hasConsole)
            {
                // If user specifically requested debug mode from GUI or shortcut, allocate a console
                hasConsole = AllocConsole();
            }

            if (hasConsole)
            {
                try
                {
                    Console.OutputEncoding = Encoding.UTF8;
                }
                catch { }
            }

            try
            {
                string baseDir = AppDomain.CurrentDomain.BaseDirectory;
                string jarPath = Path.Combine(baseDir, "DigiViewSystem.jar");
                string logFile = Path.Combine(baseDir, "digiview-runtime.log");

                if (!File.Exists(jarPath))
                {
                    string missingMsg = "Cannot find DigiViewSystem.jar in application directory:\n" + baseDir;
                    if (hasConsole)
                    {
                        Console.ForegroundColor = ConsoleColor.Red;
                        Console.Error.WriteLine("[FATAL] " + missingMsg);
                        Console.ResetColor();
                    }
                    MessageBox.Show(
                        missingMsg,
                        "DigiView LMS - File Missing",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error
                    );
                    return 1;
                }

                // Resolve JVM across all sources (Bundled JRE, JAVA_HOME, Registry, Program Files, PATH)
                string resolutionSource;
                string javaExe = ResolveJavaExecutable(baseDir, hasConsole || isDebug, out resolutionSource);

                if (string.IsNullOrEmpty(javaExe))
                {
                    string notFoundMsg = "DigiView LMS requires a Java Virtual Machine (JVM 8, 11, 17, 21+) to run.\n\n" +
                                         "No valid Java runtime was detected on your computer.\n\n" +
                                         "Please install Java (e.g., Eclipse Temurin LTS from https://adoptium.net)\n" +
                                         "or download the full DigiView-Portable package with bundled JRE.";

                    if (hasConsole)
                    {
                        Console.ForegroundColor = ConsoleColor.Red;
                        Console.Error.WriteLine("[ERROR] " + notFoundMsg);
                        Console.ResetColor();
                    }

                    MessageBox.Show(
                        notFoundMsg,
                        "DigiView LMS - Java Not Found",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning
                    );
                    return 1;
                }

                // Proactively verify & configure Windows Defender Firewall permissions for DigiView & Java
                AutoConfigureFirewall(baseDir, javaExe, hasConsole || isDebug);

                if (hasConsole || isDebug)
                {
                    Console.WriteLine("===============================================================================");
                    Console.WriteLine("  DigiView LMS -- Native Runtime Log Listener & Debug Monitor");
                    Console.WriteLine("  Base Directory : " + baseDir);
                    Console.WriteLine("  Java Executable: " + javaExe);
                    Console.WriteLine("  JVM Source     : " + resolutionSource);
                    Console.WriteLine("  Target JAR     : " + jarPath);
                    Console.WriteLine("  Log File       : " + logFile);
                    Console.WriteLine("  Timestamp      : " + DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"));
                    Console.WriteLine("===============================================================================");
                    Console.WriteLine("[INFO] Actively streaming runtime logs from DigiView process below...\n");

                    ProcessStartInfo psi = new ProcessStartInfo();
                    psi.FileName = javaExe;
                    psi.Arguments = "-Dhttps.protocols=TLSv1.2,TLSv1.3 -Djsse.enableSNIExtension=true -Dsun.security.ssl.allowUnsafeRenegotiation=true -Ddigiview.debug=true -cp \"DigiViewSystem.jar\" System.DigiView";
                    psi.WorkingDirectory = baseDir;
                    psi.UseShellExecute = false;
                    psi.RedirectStandardOutput = true;
                    psi.RedirectStandardError = true;
                    psi.CreateNoWindow = true;

                    using (StreamWriter logWriter = new StreamWriter(new FileStream(logFile, FileMode.Append, FileAccess.Write, FileShare.ReadWrite), Encoding.UTF8))
                    {
                        logWriter.WriteLine(string.Format("\n========== Session Started: {0:yyyy-MM-dd HH:mm:ss} ==========", DateTime.Now));
                        logWriter.WriteLine(string.Format("JVM: {0} ({1})", javaExe, resolutionSource));
                        logWriter.Flush();

                        using (Process process = new Process())
                        {
                            process.StartInfo = psi;

                            process.OutputDataReceived += (sender, e) =>
                            {
                                if (e.Data != null)
                                {
                                    Console.WriteLine(e.Data);
                                    try
                                    {
                                        lock (logWriter)
                                        {
                                            logWriter.WriteLine(e.Data);
                                            logWriter.Flush();
                                        }
                                    }
                                    catch { }
                                }
                            };

                            process.ErrorDataReceived += (sender, e) =>
                            {
                                if (e.Data != null)
                                {
                                    Console.ForegroundColor = ConsoleColor.Red;
                                    Console.Error.WriteLine(e.Data);
                                    Console.ResetColor();
                                    try
                                    {
                                        lock (logWriter)
                                        {
                                            logWriter.WriteLine("[STDERR] " + e.Data);
                                            logWriter.Flush();
                                        }
                                    }
                                    catch { }
                                }
                            };

                            process.Start();
                            process.BeginOutputReadLine();
                            process.BeginErrorReadLine();

                            // Retain terminal session and actively listen until DigiView window closes
                            process.WaitForExit();

                            Console.WriteLine("\n===============================================================================");
                            Console.WriteLine("  [INFO] DigiView LMS application closed. Exit code: " + process.ExitCode);
                            Console.WriteLine("  [INFO] Logs preserved in: " + logFile);
                            Console.WriteLine("===============================================================================");

                            logWriter.WriteLine(string.Format("========== Session Ended: {0:yyyy-MM-dd HH:mm:ss} (Exit: {1}) ==========\n", DateTime.Now, process.ExitCode));
                            logWriter.Flush();

                            return process.ExitCode;
                        }
                    }
                }
                else
                {
                    // Silent GUI Launch (Explorer double-click)
                    ProcessStartInfo psi = new ProcessStartInfo();
                    psi.FileName = javaExe;
                    psi.Arguments = "-Dhttps.protocols=TLSv1.2,TLSv1.3 -Djsse.enableSNIExtension=true -Dsun.security.ssl.allowUnsafeRenegotiation=true -cp \"DigiViewSystem.jar\" System.DigiView";
                    psi.WorkingDirectory = baseDir;
                    psi.UseShellExecute = false;
                    psi.CreateNoWindow = true;

                    Process.Start(psi);
                    return 0;
                }
            }
            catch (Exception ex)
            {
                if (hasConsole)
                {
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.Error.WriteLine("[DigiView Launcher Exception] " + ex.ToString());
                    Console.ResetColor();
                }
                MessageBox.Show(
                    "Failed to launch DigiView LMS:\n\n" + ex.Message + "\n\nPlease ensure a valid Java Runtime (JVM 8, 11, 17, 21+) or the bundled JRE is available.",
                    "DigiView LMS - Launch Error",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
                return 1;
            }
        }

        /// <summary>
        /// Universally locates a Java executable across all possible sources on Windows.
        /// </summary>
        private static string ResolveJavaExecutable(string baseDir, bool consoleMode, out string source)
        {
            string targetExe = consoleMode ? "java.exe" : "javaw.exe";
            source = "Unknown";

            // Priority 1: Application-bundled zero-install JRE
            string bundledPath = Path.Combine(baseDir, Path.Combine("jre", Path.Combine("bin", targetExe)));
            if (File.Exists(bundledPath))
            {
                source = "Bundled Zero-Install JRE";
                return bundledPath;
            }

            // Priority 2: JAVA_HOME environment variable
            string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
            if (!string.IsNullOrEmpty(javaHome))
            {
                string p = Path.Combine(javaHome, Path.Combine("bin", targetExe));
                if (File.Exists(p)) { source = "JAVA_HOME (" + javaHome + ")"; return p; }
                p = Path.Combine(javaHome, Path.Combine("jre", Path.Combine("bin", targetExe)));
                if (File.Exists(p)) { source = "JAVA_HOME/jre (" + javaHome + ")"; return p; }
            }

            // Priority 3: JRE_HOME or JDK_HOME
            string jreHome = Environment.GetEnvironmentVariable("JRE_HOME");
            if (!string.IsNullOrEmpty(jreHome))
            {
                string p = Path.Combine(jreHome, Path.Combine("bin", targetExe));
                if (File.Exists(p)) { source = "JRE_HOME (" + jreHome + ")"; return p; }
            }
            string jdkHome = Environment.GetEnvironmentVariable("JDK_HOME");
            if (!string.IsNullOrEmpty(jdkHome))
            {
                string p = Path.Combine(jdkHome, Path.Combine("bin", targetExe));
                if (File.Exists(p)) { source = "JDK_HOME (" + jdkHome + ")"; return p; }
            }

            // Priority 4: Windows Registry (JavaSoft, Eclipse Adoptium, AdoptOpenJDK)
            string regPath = QueryJavaRegistry(targetExe, out source);
            if (!string.IsNullOrEmpty(regPath) && File.Exists(regPath))
                return regPath;

            // Priority 5: Scan standard Program Files locations for popular JDK/JRE distributions
            string progPath = ScanProgramFiles(targetExe, out source);
            if (!string.IsNullOrEmpty(progPath) && File.Exists(progPath))
                return progPath;

            // Priority 6: Check system PATH
            string pathMatch = SearchPath(targetExe);
            if (!string.IsNullOrEmpty(pathMatch) && File.Exists(pathMatch))
            {
                source = "System PATH (" + pathMatch + ")";
                return pathMatch;
            }

            // If targetExe is javaw.exe but only java.exe exists in PATH (or vice-versa), check alternative
            string altExe = consoleMode ? "javaw.exe" : "java.exe";
            string altMatch = SearchPath(altExe);
            if (!string.IsNullOrEmpty(altMatch) && File.Exists(altMatch))
            {
                source = "System PATH (" + altMatch + ")";
                return altMatch;
            }

            return null;
        }

        private static string QueryJavaRegistry(string targetExe, out string source)
        {
            source = "Registry";
            string[] keys = {
                @"SOFTWARE\JavaSoft\Java Runtime Environment",
                @"SOFTWARE\JavaSoft\Java Development Kit",
                @"SOFTWARE\JavaSoft\JDK",
                @"SOFTWARE\Eclipse Adoptium\JRE",
                @"SOFTWARE\Eclipse Adoptium\JDK",
                @"SOFTWARE\AdoptOpenJDK\JRE",
                @"SOFTWARE\AdoptOpenJDK\JDK"
            };

            foreach (string keyName in keys)
            {
                try
                {
                    using (RegistryKey root = Registry.LocalMachine.OpenSubKey(keyName))
                    {
                        if (root != null)
                        {
                            string currentVer = root.GetValue("CurrentVersion") as string;
                            if (!string.IsNullOrEmpty(currentVer))
                            {
                                using (RegistryKey verKey = root.OpenSubKey(currentVer))
                                {
                                    if (verKey != null)
                                    {
                                        string jh = verKey.GetValue("JavaHome") as string;
                                        if (!string.IsNullOrEmpty(jh))
                                        {
                                            string exePath = Path.Combine(jh, Path.Combine("bin", targetExe));
                                            if (File.Exists(exePath))
                                            {
                                                source = "Windows Registry (" + keyName + " v" + currentVer + ")";
                                                return exePath;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch { }
            }
            return null;
        }

        private static string ScanProgramFiles(string targetExe, out string source)
        {
            source = "Program Files";
            string[] roots = {
                Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles),
                Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86)
            };

            string[] vendors = {
                "Java",
                "Eclipse Adoptium",
                "AdoptOpenJDK",
                "Microsoft",
                "Amazon Corretto",
                "Zulu",
                "BellSoft",
                "Semeru"
            };

            foreach (string root in roots)
            {
                if (string.IsNullOrEmpty(root) || !Directory.Exists(root)) continue;
                foreach (string vendor in vendors)
                {
                    string vendorPath = Path.Combine(root, vendor);
                    if (Directory.Exists(vendorPath))
                    {
                        try
                        {
                            string[] subDirs = Directory.GetDirectories(vendorPath);
                            Array.Sort(subDirs);
                            Array.Reverse(subDirs); // Test newer versions first
                            foreach (string dir in subDirs)
                            {
                                string candidate = Path.Combine(dir, Path.Combine("bin", targetExe));
                                if (File.Exists(candidate))
                                {
                                    source = vendor + " (" + Path.GetFileName(dir) + ")";
                                    return candidate;
                                }
                                candidate = Path.Combine(dir, Path.Combine("jre", Path.Combine("bin", targetExe)));
                                if (File.Exists(candidate))
                                {
                                    source = vendor + " JRE (" + Path.GetFileName(dir) + ")";
                                    return candidate;
                                }
                            }
                        }
                        catch { }
                    }
                }
            }
            return null;
        }

        private static string SearchPath(string fileName)
        {
            string pathVar = Environment.GetEnvironmentVariable("PATH");
            if (string.IsNullOrEmpty(pathVar)) return null;

            foreach (string p in pathVar.Split(';'))
            {
                string trimmed = p.Trim();
                if (!string.IsNullOrEmpty(trimmed) && Directory.Exists(trimmed))
                {
                    try
                    {
                        string full = Path.Combine(trimmed, fileName);
                        if (File.Exists(full)) return full;
                    }
                    catch { }
                }
            }
            return null;
        }

        /// <summary>
        /// Proactively verifies and registers Windows Defender Firewall rules for DigiView and Java.
        /// Also performs a pre-flight network connectivity check for the end user.
        /// </summary>
        private static void AutoConfigureFirewall(string baseDir, string javaExe, bool logToConsole)
        {
            try
            {
                string exePath = Process.GetCurrentProcess().MainModule.FileName;
                bool ruleExists = false;

                try
                {
                    ProcessStartInfo checkPsi = new ProcessStartInfo("netsh", "advfirewall firewall show rule name=\"DigiView LMS Application (Outbound)\"");
                    checkPsi.CreateNoWindow = true;
                    checkPsi.UseShellExecute = false;
                    checkPsi.RedirectStandardOutput = true;
                    using (Process p = Process.Start(checkPsi))
                    {
                        string output = p.StandardOutput.ReadToEnd();
                        p.WaitForExit();
                        if (output.Contains("DigiView LMS Application")) ruleExists = true;
                    }
                }
                catch { }

                if (ruleExists)
                {
                    if (logToConsole)
                    {
                        Console.WriteLine("[FIREWALL] Windows Defender Firewall rules: ALREADY CONFIGURED (Outbound/Inbound OK)");
                    }
                }
                else
                {
                    if (logToConsole)
                    {
                        Console.WriteLine("[FIREWALL] Checking and registering Windows Defender Firewall rules...");
                    }

                    string[] progs = new string[] { exePath, javaExe };
                    foreach (string prog in progs)
                    {
                        if (string.IsNullOrEmpty(prog) || !File.Exists(prog)) continue;
                        string fname = Path.GetFileName(prog);
                        RunSilentCommand("netsh", string.Format("advfirewall firewall add rule name=\"DigiView LMS ({0} Outbound)\" dir=out action=allow program=\"{1}\" enable=yes", fname, prog));
                        RunSilentCommand("netsh", string.Format("advfirewall firewall add rule name=\"DigiView LMS ({0} Inbound)\" dir=in action=allow program=\"{1}\" enable=yes", fname, prog));
                    }
                    RunSilentCommand("netsh", "advfirewall firewall add rule name=\"DigiView LMS Port 8085 (Inbound)\" dir=in action=allow protocol=TCP localport=8085 enable=yes");
                    RunSilentCommand("netsh", "advfirewall firewall add rule name=\"DigiView LMS Port 8085 (Outbound)\" dir=out action=allow protocol=TCP localport=8085 enable=yes");
                }

                // Pre-flight cloud server connectivity check
                if (logToConsole)
                {
                    CheckCloudConnectivity();
                }
            }
            catch { }
        }

        private static void CheckCloudConnectivity()
        {
            try
            {
                Console.Write("[NETWORK] Checking DigiView Cloud Server connectivity... ");
                System.Diagnostics.Stopwatch sw = System.Diagnostics.Stopwatch.StartNew();
                ServicePointManager.SecurityProtocol = (SecurityProtocolType)3072 | SecurityProtocolType.Tls12;
                HttpWebRequest req = (HttpWebRequest)WebRequest.Create("https://digi-view-system.onrender.com/health");
                req.Timeout = 4000;
                req.Method = "GET";
                using (HttpWebResponse res = (HttpWebResponse)req.GetResponse())
                {
                    sw.Stop();
                    Console.ForegroundColor = ConsoleColor.Green;
                    Console.WriteLine("ONLINE (HTTP {0}, {1}ms)", (int)res.StatusCode, sw.ElapsedMilliseconds);
                    Console.ResetColor();
                }
            }
            catch (Exception ex)
            {
                Console.ForegroundColor = ConsoleColor.Yellow;
                Console.WriteLine("NOTICE ({0})", ex.Message);
                Console.ResetColor();
            }
        }

        private static void RunSilentCommand(string fileName, string args)
        {
            try
            {
                ProcessStartInfo psi = new ProcessStartInfo(fileName, args);
                psi.CreateNoWindow = true;
                psi.UseShellExecute = false;
                using (Process p = Process.Start(psi))
                {
                    p.WaitForExit(3000);
                }
            }
            catch { }
        }
    }
}
