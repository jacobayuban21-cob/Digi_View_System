using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

namespace DigiViewLauncher
{
    /// <summary>
    /// Native Windows GUI launcher for DigiView Portable.
    /// Runs silently without a command prompt window, locating the bundled JRE
    /// or falling back to the system JRE.
    /// </summary>
    static class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            try
            {
                string baseDir = AppDomain.CurrentDomain.BaseDirectory;
                string jarPath = Path.Combine(baseDir, "DigiViewSystem.jar");
                string bundledJavaw = Path.Combine(baseDir, "jre", "bin", "javaw.exe");

                if (!File.Exists(jarPath))
                {
                    MessageBox.Show(
                        "Cannot find DigiViewSystem.jar in the application directory:\n" + baseDir,
                        "DigiView LMS - File Missing",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error
                    );
                    return;
                }

                string javaExecutable = File.Exists(bundledJavaw) ? bundledJavaw : "javaw.exe";

                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = javaExecutable;
                psi.Arguments = "-cp \"DigiViewSystem.jar\" System.DigiView";
                psi.WorkingDirectory = baseDir;
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;

                Process.Start(psi);
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    "Failed to launch DigiView LMS:\n\n" + ex.Message + "\n\nPlease ensure Java 17+ or the bundled JRE is available.",
                    "DigiView LMS - Launch Error",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
            }
        }
    }
}
