using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

using Google.Apis.Auth.OAuth2;
using Google.Apis.Drive.v3;
using Google.Apis.Drive.v3.Data;
using Google.Apis.Services;
using Google.Apis.Util.Store;
using Google.Apis.Download;
using System.Reflection;
using System.Windows;
using System.Web.Script.Serialization;
using System.Text.RegularExpressions;

namespace GalaxyRP_Launcher
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
            tabControl1.TabPages[0].Text = "File Manager";
            tabControl1.TabPages[1].Text = "Launcher Settings";

            string json = System.IO.File.ReadAllText("launcher_config.cfg");
            JavaScriptSerializer serializer = new JavaScriptSerializer();
            Dictionary<string, string> dic = serializer.Deserialize<Dictionary<string, string>>(json);

            GetSettingsFromConfig(dic);
        }

        IList<Google.Apis.Drive.v3.Data.File> files;
        string filepath = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location) + "\\base";
        string googleDriveFolderId = "1krZva8NV7BBDsivrRiu0keOfQ-y_pEyS";
        string googleDriveLink = "";
        int resolution_x = 0;
        int resolution_y = 0;
        string clientMod = "BaseJKA";
        string serverIP = "86.160.153.128:27340";

        string GetFolderIdFromLink(string link)
        {
            string searchString = link;
            string theValue = string.Empty;
            string theDescription = string.Empty;
            string theLevel = string.Empty;
            string pattern = ".*[/=]([01A-Z][-_[:alnum:]]+)([?/].*|$)"; // continue the pattern for your needs
            Regex rx = new Regex(pattern);

            Match m = rx.Match(searchString);

            if (m.Success)
            {
                string folderId = m.Groups[0].Value;
                return folderId;
            }

            return 0.ToString();
        }

        void GetSettingsFromConfig(Dictionary<string, string> dictionary)
        {
            resolution_x = Int32.Parse(dictionary["resolution_x"]);
            resolution_y = Int32.Parse(dictionary["resolution_y"]);
            clientMod = dictionary["clientMod"];
            googleDriveLink = dictionary["googleDriveLink"];
            serverIP = dictionary["serverIP"];

            textBox_resolution_x.Text = resolution_x.ToString();
            textBox_resolution_y.Text = resolution_y.ToString();
            textBox_server_ip.Text = serverIP;
            comboBox_client_mod.Text = clientMod;
            textBox_google_drive_link.Text = googleDriveLink;
            googleDriveFolderId = googleDriveLink.Substring(googleDriveLink.Length - 33);
        }

        IList<Google.Apis.Drive.v3.Data.File> ProcessFileList(IList<Google.Apis.Drive.v3.Data.File> originalFileList)
        {

            for(int i = 0; i < originalFileList.Count; i++)
            {
                if (originalFileList[i].FullFileExtension != "pk3")
                {
                    originalFileList.RemoveAt(i);
                    i--;
                }
            }

            return originalFileList;
        }

        private string GetCurrentSelectedFileName()
        {
            return files[listBox1.SelectedIndex].Name;
        }
        private string GetCurrentSelectedFileId()
        {
            return files[listBox1.SelectedIndex].Id;
        }

        private void button1_Click(object sender, EventArgs e)
        {
             var filelist = GetFileList();
        }

        private async Task<DriveService> CreateService()
        {
            UserCredential credential;
            using (var stream = new FileStream("client_secrets.json", FileMode.Open, FileAccess.Read))
            {

                credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(
                    GoogleClientSecrets.Load(stream).Secrets,
                    new[] { DriveService.Scope.DriveReadonly },
                    "user", CancellationToken.None, new FileDataStore("Drive.ListFiles"));
            }

            DriveService service = new DriveService(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "GalaxyRP Launcher",
            });

            return service;
        }

        private async Task<IList<Google.Apis.Drive.v3.Data.File>> GetFileList()
        {
            DriveService service = await CreateService();

            var testRequest = service.Files.List();
            testRequest.SupportsAllDrives = true;
            testRequest.SupportsTeamDrives = true;
            testRequest.IncludeItemsFromAllDrives = true;
            testRequest.Q = "parents in '" + googleDriveFolderId +"'";
            testRequest.Fields = "*";

            try
            {
                FileList filelist = testRequest.Execute();
                files = filelist.Files;
            }
            catch (Exception e)
            {
                int i = 0;
            }


            files = ProcessFileList(files);

            listBox1.Items.Clear();

            foreach (Google.Apis.Drive.v3.Data.File file in files)
            {
                listBox1.Items.Add(file.Name);

            }
            return files;
        }

        private long GetSizeOfSelectedItem()
        {
            return (long)files[listBox1.SelectedIndex].Size;
        }

        // Asynchronous event handler
        private async void StartDownloadAsync(string fileId)
        {
            DriveService service = await CreateService();

            //var fileId = "1QETWTnkIp9q6O35Rm99qC6LsJ4Gdg3I5";

            progressBar1.Minimum = 0;
            progressBar1.Maximum = 100;
            progressBar1.Value = 0;

            // Creating an instance of Progress<T> captures the current 
            // SynchronizationContext (UI context) to prevent cross threading when updating the ProgressBar
            IProgress<double> progressReporter =
              new Progress<double>(value => progressBar1.Value =(int) value);

            await DownloadAsync(progressReporter, fileId);
        }


        private async Task DownloadAsync(IProgress<double>  progressReporter, string fileId)
        {
            DriveService service = await CreateService();

            var streamDownload = new MemoryStream();

            var request = service.Files.Get(fileId);
            var file = request.Execute();
            //long? fileSize = file.Size;
            long? fileSize = GetSizeOfSelectedItem();

            // Report progress to UI via the captured UI's SynchronizationContext using IProgress<T>
            request.MediaDownloader.ProgressChanged +=
              (progress) => ReportProgress(progress, progressReporter, fileSize, streamDownload, file.Name);

            // Execute download asynchronous
            await Task.Run(() => request.Download(streamDownload));
        }


        private void ReportProgress(IDownloadProgress progress, IProgress<double> progressReporter, long? fileSize, MemoryStream streamDownload, string fileName)
        {
            switch (progress.Status)
            {
                case DownloadStatus.Downloading:
                    {
                        double progressValue = Convert.ToDouble(progress.BytesDownloaded * 100 / fileSize);

                        // Update the ProgressBar on the UI thread
                        progressReporter.Report(progressValue);
                        //progressBar1.Value = (int)(progress.BytesDownloaded * 100 / fileSize);
                        break;
                    }
                case DownloadStatus.Completed:
                    {
                        Console.WriteLine("Download complete.");
                        using (FileStream fs = new FileStream(filepath + "\\" + fileName, FileMode.OpenOrCreate))
                        {
                            progressReporter.Report(100);
                            streamDownload.WriteTo(fs);
                            fs.Flush();
                        }
                        break;
                    }
                case DownloadStatus.Failed:
                    {
                        break;
                    }
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {

            string selectedFileId = GetCurrentSelectedFileId();

            StartDownloadAsync(selectedFileId);
            
        }

        private void listBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            label_filename.Text = files[listBox1.SelectedIndex].Name;
            label_filesize.Text = (files[listBox1.SelectedIndex].Size / 1000000).ToString() + "MB";
            label_version_number.Text = files[listBox1.SelectedIndex].Version.ToString();
            label_author.Text = files[listBox1.SelectedIndex].Owners[0].DisplayName;
            label_last_changed.Text = files[listBox1.SelectedIndex].ModifiedTime.ToString();
        }
    }
}
