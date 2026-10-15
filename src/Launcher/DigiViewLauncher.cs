using System;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Forms;

namespace DigiViewLauncher
{
    /// <summary>
    /// Native Windows Launcher for DigiView LMS Portable.
    /// Dual-mode execution:
    /// 1. Silent GUI Mode (default on double-click in Explorer): runs without a terminal window.
    /// 2. Active Log-Listening Console Mode (when called from DigiView-Debug.bat, cmd, or with --debug):
    ///    attaches to the calling console or allocates one, actively streams all Java runtime logs
    ///    in real-time, writes to digiview-runtime.log, and retains the console until the app exits.
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
                string bundledJava = Path.Combine(baseDir, "jre", "bin", "java.exe");
                string bundledJavaw = Path.Combine(baseDir, "jre", "bin", "javaw.exe");
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

                // In debug or console mode, use java.exe so streams are live; otherwise use javaw.exe
                string javaExe;
                if (hasConsole || isDebug)
                {
                    javaExe = File.Exists(bundledJava) ? bundledJava : "java.exe";
                }
                else
                {
                    javaExe = File.Exists(bundledJavaw) ? bundledJavaw : "javaw.exe";
                }

                if (hasConsole || isDebug)
                {
                    Console.WriteLine("===============================================================================");
                    Console.WriteLine("  DigiView LMS -- Native Runtime Log Listener & Debug Monitor");
                    Console.WriteLine("  Base Directory : " + baseDir);
                    Console.WriteLine("  Java Executable: " + javaExe);
                    Console.WriteLine("  Target JAR     : " + jarPath);
                    Console.WriteLine("  Log File       : " + logFile);
                    Console.WriteLine("  Timestamp      : " + DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"));
                    Console.WriteLine("===============================================================================");
                    Console.WriteLine("[INFO] Actively streaming runtime logs from DigiView process below...\n");

                    ProcessStartInfo psi = new ProcessStartInfo();
                    psi.FileName = javaExe;
                    psi.Arguments = "-Ddigiview.debug=true -cp \"DigiViewSystem.jar\" System.DigiView";
                    psi.WorkingDirectory = baseDir;
                    psi.UseShellExecute = false;
                    psi.RedirectStandardOutput = true;
                    psi.RedirectStandardError = true;
                    psi.CreateNoWindow = true;

                    using (StreamWriter logWriter = new StreamWriter(new FileStream(logFile, FileMode.Append, FileAccess.Write, FileShare.ReadWrite), Encoding.UTF8))
                    {
                        logWriter.WriteLine(string.Format("\n========== Session Started: {0:yyyy-MM-dd HH:mm:ss} ==========", DateTime.Now));
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
                    psi.Arguments = "-cp \"DigiViewSystem.jar\" System.DigiView";
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
                    "Failed to launch DigiView LMS:\n\n" + ex.Message + "\n\nPlease ensure Java 17+ or the bundled JRE is available.",
                    "DigiView LMS - Launch Error",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
                return 1;
            }
        }
    }
}
