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
using File = System.IO.File;

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

            label_filename.Text = "";
            label_filesize.Text = "";
            label_version_number.Text = "";
            label_author.Text = "";
            label_last_changed.Text = "";
        }

        IList<Google.Apis.Drive.v3.Data.File> files;
        string filepath = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location) + "\\base";
        string googleDriveFolderId = "";
        string googleDriveLink = "";
        int resolution_x = 0;
        int resolution_y = 0;
        string clientMod = "BaseJKA";
        string serverIP = "";
        string serverName = "";
        string serverIP2 = "";
        string server2Name = "";
        string otherArguments = "";

        void compareLocalFilesWithCloud()
        {
            for(int i = 0;i<files.Count;i++)
            {
                Google.Apis.Drive.v3.Data.File file = files[i];

                // Process the list of files found in the directory.
                string[] fileEntries = Directory.GetFiles(filepath);
                foreach (string fileName in fileEntries)
                {
                    var md5 = System.Security.Cryptography.MD5.Create();
                    var stream = File.OpenRead(fileName);

                    var hash = md5.ComputeHash(stream);
                    var md5String = BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
                    if (md5String == file.Md5Checksum)
                    {
                        files.RemoveAt(i);
                        i--;
                    }
                }
            }
        }

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
            serverName = dictionary["serverName"];
            serverIP2 = dictionary["serverIP2"];
            server2Name = dictionary["server2Name"];
            otherArguments = dictionary["custom_arguments"];

            textBox_resolution_x.Text = resolution_x.ToString();
            textBox_resolution_y.Text = resolution_y.ToString();
            textBox_server_ip.Text = serverIP;
            textBox_server_name.Text = serverName;
            textBox_server_ip_2.Text = serverIP2;
            textBox_server_name_2.Text = server2Name;
            comboBox_client_mod.Text = clientMod;
            textBox_google_drive_link.Text = googleDriveLink;
            googleDriveFolderId = googleDriveLink.Substring(googleDriveLink.Length - 33);
            textBox_other_arguments.Text = otherArguments;

            comboBox_server_selection.Items.Add(serverName+ " | " + serverIP);
            if (serverIP2 != "")
            {
                comboBox_server_selection.Items.Add(server2Name + " | " + serverIP2);
            }
            comboBox_server_selection.Text = comboBox_server_selection.Items[0].ToString();
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

            compareLocalFilesWithCloud();

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
             GetFileList();
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

            if (listBox1.Items.Count > 0)
            {
                button5.Enabled = true;
            }
            else
            {
                button5.Enabled = false;
                button2.Enabled = false;
            }

            return files;
        }

        private long GetSizeOfSelectedItem()
        {
            return (long)files[listBox1.SelectedIndex].Size;
        }

        private long GetSizeOfItemWithId(string fileId)
        {
            for(int i = 0; i < files.Count; i++)
            {
                if(files[i].Id == fileId)
                {
                    return (long)files[i].Size;
                }
            }
            return 0;
        }

        // Asynchronous event handler
        private async Task StartDownloadAsync(string fileId)
        {
            DriveService service = await CreateService();

            progressBar1.Minimum = 0;
            progressBar1.Maximum = 100;
            progressBar1.Value = 0;

            // Creating an instance of Progress<T> captures the current 
            // SynchronizationContext (UI context) to prevent cross threading when updating the ProgressBar
            IProgress<double> progressReporter =
              new Progress<double>(value => progressBar1.Value =(int) value);

            await DownloadAsync(progressReporter, fileId);

            await GetFileList();
        }


        private async Task DownloadAsync(IProgress<double>  progressReporter, string fileId)
        {
            DriveService service = await CreateService();

            var streamDownload = new MemoryStream();

            var request = service.Files.Get(fileId);
            var file = request.Execute();
            //long? fileSize = file.Size;
            long? fileSize = GetSizeOfItemWithId(fileId);

            // Report progress to UI via the captured UI's SynchronizationContext using IProgress<T>
            request.MediaDownloader.ProgressChanged +=
              (progress) => ReportProgress(progress, progressReporter, fileSize, streamDownload, file.Name);

            // Execute download asynchronous
            await Task.Run(() => request.Download(streamDownload));
        }

        void LockControls()
        {
            button1.Enabled = false;
            button2.Enabled = false;
            button4.Enabled = false;
            button5.Enabled = false;
            listBox1.Enabled = false;
            textBox_google_drive_link.Enabled = false;
        }

        void UnlockControls()
        {
            button1.Enabled = true;
            button4.Enabled = true;
            button5.Enabled = true;
            listBox1.Enabled = true;
            textBox_google_drive_link.Enabled = true;

            if (listBox1.SelectedIndex != -1)
            {
                button2.Enabled = true;
            }
        }

        private void ReportProgress(IDownloadProgress progress, IProgress<double> progressReporter, long? fileSize, MemoryStream streamDownload, string fileName)
        {
            switch (progress.Status)
            {
                case DownloadStatus.Downloading:
                    {
                        double progressValue = Convert.ToDouble(progress.BytesDownloaded * 100 / fileSize);
                        if(progressValue > 100)
                        {
                            progressValue = 100;
                        }
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

        private async void button2_Click(object sender, EventArgs e)
        {
            string selectedFileId = GetCurrentSelectedFileId();
            LockControls();
            await StartDownloadAsync(selectedFileId);
            UnlockControls();
        }

        private void UpdateFileDetails(int fileIndex)
        {
            label_filename.Text = files[fileIndex].Name;
            label_filesize.Text = (files[fileIndex].Size / 1000000).ToString() + "MB";
            label_version_number.Text = files[fileIndex].Version.ToString();
            label_author.Text = files[fileIndex].Owners[0].DisplayName;
            label_last_changed.Text = files[fileIndex].ModifiedTime.ToString();
        }

        private void listBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            UpdateFileDetails(listBox1.SelectedIndex);

            button2.Enabled = true;
            button5.Enabled = true;
        }

        private void button3_Click(object sender, EventArgs e)
        {

        }

        private async void button5_Click(object sender, EventArgs e)
        {
            LockControls();

            while(listBox1.Items.Count != 0)
            {
                UpdateFileDetails(0);
                await StartDownloadAsync(files[0].Id);

            }
            UnlockControls();
        }
    }
}
