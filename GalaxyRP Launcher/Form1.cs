using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.IO;
using System.Threading;

using Google.Apis.Auth.OAuth2;
using Google.Apis.Drive.v3;
using Google.Apis.Drive.v3.Data;
using Google.Apis.Services;
using Google.Apis.Util.Store;
using Google.Apis.Download;
using System.Reflection;
using File = System.IO.File;
using Newtonsoft.Json;
using System.Diagnostics;
using System.Text.RegularExpressions;

namespace GalaxyRP_Launcher
{
    public partial class Form1 : Form
    {
        //GalaxyRP (Alex): Location of the base folder.
        string filepath = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location) + "\\base";
        //GalaxyRP (Alex): This variable will store all the file metadata we get from Google drive.
        public IList<Google.Apis.Drive.v3.Data.File> files { get; set; }
        //GalaxyRP (Alex): Id of the drive folder we'll be interacting with.
        public string googleDriveFolderId { get; set; }

        LauncherConfig currentConfiguration = new LauncherConfig();

        public Form1()
        {
            InitializeComponent();
            tabControl1.TabPages[0].Text = "File Manager";
            tabControl1.TabPages[1].Text = "Launcher Settings";

            if (!File.Exists("launcher_config.cfg"))
            {
                buildDefaultConfig();
                
            }
            string json = System.IO.File.ReadAllText("launcher_config.cfg");
            GetSettingsFromConfig(json);

            label_filename.Text = "";
            label_filesize.Text = "";
            label_version_number.Text = "";
            label_author.Text = "";
            label_last_changed.Text = "";

            RefreshControls();
        }

        //GalaxyRP (Alex): Create an empty config file.
        void buildDefaultConfig()
        {
            currentConfiguration.serverIP = "";
            currentConfiguration.serverName = "";
            currentConfiguration.serverIP2 = "";
            currentConfiguration.server2Name = "";
            currentConfiguration.googleDriveLink = "";
            currentConfiguration.clientMod = "";
            currentConfiguration.resolution_x = 1280;
            currentConfiguration.resolution_y = 720;
            currentConfiguration.otherArguments = "";

            string json = JsonConvert.SerializeObject(currentConfiguration);
            //write string to file
            System.IO.File.WriteAllText("launcher_config.cfg", json);
        }

        //GalaxyRP (Alex): Checks the checksum of each file against the ones in the cloud. (The ones stored in files that were grabbed via the api)
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
                    stream.Dispose();
                    stream.Close();
                }
            }
        }

        //GalaxyRP (Alex): Reads the config json, and fills in all the variables and text fields in the app.
        void GetSettingsFromConfig(string json)
        {
            currentConfiguration = JsonConvert.DeserializeObject<LauncherConfig>(json);

            textBox_resolution_x.Text = currentConfiguration.resolution_x.ToString();
            textBox_resolution_y.Text = currentConfiguration.resolution_y.ToString();
            textBox_server_ip.Text = currentConfiguration.serverIP;
            textBox_server_name.Text = currentConfiguration.serverName;
            textBox_server_ip_2.Text = currentConfiguration.serverIP2;
            textBox_server_name_2.Text = currentConfiguration.server2Name;
            comboBox_client_mod.Text = currentConfiguration.clientMod;
            textBox_google_drive_link.Text = currentConfiguration.googleDriveLink;
            if (currentConfiguration.googleDriveLink.Length > 34)
            {
                googleDriveFolderId = currentConfiguration.googleDriveLink.Substring(currentConfiguration.googleDriveLink.Length - 33);
            }
            else
            {
                googleDriveFolderId = null;
            }
            textBox_other_arguments.Text = currentConfiguration.otherArguments;

            comboBox_server_selection.Items.Clear();
            if (currentConfiguration.serverIP != "")
            {
                comboBox_server_selection.Items.Add(currentConfiguration.serverName + " | " + currentConfiguration.serverIP);
            }
            if (currentConfiguration.serverIP2 != "")
            {
                comboBox_server_selection.Items.Add(currentConfiguration.server2Name + " | " + currentConfiguration.serverIP2);
            }
            if (comboBox_server_selection.Items.Count > 0)
            {
                comboBox_server_selection.Text = comboBox_server_selection.Items[0].ToString();
            }
        }
        //GalaxyRP (Alex): Saves the current stored config in memory, and also writes it to the config file.
        void saveConfig()
        {
            currentConfiguration.serverIP = textBox_server_ip.Text;
            currentConfiguration.serverName = textBox_server_name.Text;
            currentConfiguration.serverIP2 = textBox_server_ip_2.Text;
            currentConfiguration.server2Name = textBox_server_name_2.Text;
            currentConfiguration.googleDriveLink = textBox_google_drive_link.Text;
            currentConfiguration.clientMod = comboBox_client_mod.Text;
            currentConfiguration.resolution_x = Int32.Parse(textBox_resolution_x.Text);
            currentConfiguration.resolution_y = Int32.Parse(textBox_resolution_y.Text);
            currentConfiguration.otherArguments = textBox_other_arguments.Text;


            string json = JsonConvert.SerializeObject(currentConfiguration);
            //write string to file
            System.IO.File.WriteAllText("launcher_config.cfg", json);
            GetSettingsFromConfig(json);
        }

        //GalaxyRP (Alex): Filters the response form Google drive, and makes sure we only get pk3 files, and that those files are updates that we don't have.
        IList<Google.Apis.Drive.v3.Data.File> FilterFileList(IList<Google.Apis.Drive.v3.Data.File> originalFileList)
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

        //GalaxyRP (Alex): Return the Id of the file that's currently selected in listBox1
        private string GetCurrentSelectedFileId()
        {
            return files[listBox1.SelectedIndex].Id;
        }

        private async void button1_Click(object sender, EventArgs e)
        {
            LockControls();
            await GetFileList();
            UnlockControls();
        }

        //GalaxyRP (Alex): Connects to Google Drive via the api key.
        private async Task<DriveService> CreateService()
        {
            UserCredential credential;
#if DEBUG
            using (var stream = new FileStream("client_secrets.json", FileMode.Open, FileAccess.Read))
            {

                credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(
                    GoogleClientSecrets.Load(stream).Secrets,
                    new[] { DriveService.Scope.DriveReadonly },
                    "user", CancellationToken.None, new FileDataStore("Drive.ListFiles"));
            }
#endif
            //GalaxyRP (Alex): When releasing, hard code the client secret in so that you don't have to share it. NEVER EVER push a commit where this is shown.

#if !DEBUG
            string clientSecrets = "";

            byte[] byteArray = System.Text.Encoding.ASCII.GetBytes(clientSecrets);
            MemoryStream stream = new MemoryStream(byteArray);

            credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(
                    GoogleClientSecrets.Load(stream).Secrets,
                    new[] { DriveService.Scope.DriveReadonly },
                    "user", CancellationToken.None, new FileDataStore("Drive.ListFiles"));
#endif

            DriveService service = new DriveService(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "GalaxyRP Launcher",
            });

            return service;
        }

        //GalaxyRP (Alex): Searches google drive for all files, then applies a filter on those. Also fills in listBox1 with values.
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


            files = FilterFileList(files);

            listBox1.Items.Clear();

            foreach (Google.Apis.Drive.v3.Data.File file in files)
            {
                listBox1.Items.Add(file.Name);

            }

            return files;
        }

        //GalaxyRP (Alex): Given an Id, search files for the size in bytes of the item with that id.
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

        //GalaxyRP (Alex): ASYNCHRONOUS Given a file id, starts to download it from Google Drive.
        private async Task StartDownloadAsync(string fileId)
        {
            DriveService service = await CreateService();

            progressBar1.Minimum = 0;
            progressBar1.Maximum = 100;
            progressBar1.Value = 0;

            //GalaxyRP (Alex): Creating an instance of Progress<T> captures the current 
            //GalaxyRP (Alex): SynchronizationContext (UI context) to prevent cross threading when updating the ProgressBar
            IProgress<double> progressReporter =
              new Progress<double>(value => progressBar1.Value =(int) value);

            await DownloadAsync(progressReporter, fileId);

            await GetFileList();
        }

        //GalaxyRP (Alex): Manages the ongoing download process.
        private async Task DownloadAsync(IProgress<double>  progressReporter, string fileId)
        {
            DriveService service = await CreateService();

            var streamDownload = new MemoryStream();

            var request = service.Files.Get(fileId);
            var file = request.Execute();
            long? fileSize = GetSizeOfItemWithId(fileId);

            // Report progress to UI via the captured UI's SynchronizationContext using IProgress<T>
            request.MediaDownloader.ProgressChanged +=
              (progress) => ReportProgress(progress, progressReporter, fileSize, streamDownload, file.Name);

            // Execute download asynchronous
            await Task.Run(() => request.Download(streamDownload));
        }

        //GalaxyRP (Alex): Simply a shortcut to perform all the checks necessary to see if buttons that are active should still be active.
        void RefreshControls()
        {
            LockControls();
            UnlockControls();
        }

        //GalaxyRP (Alex): Locks important buttons so that nothing interferes with the download process.
        void LockControls()
        {
            button1.Enabled = false;
            button2.Enabled = false;
            button4.Enabled = false;
            button5.Enabled = false;
            listBox1.Enabled = false;
            textBox_google_drive_link.Enabled = false;
        }
        
        //GalaxyRP (Alex): Unlocks important buttons. Also takes care to not unlock buttons that require other actions to be performed first.
        void UnlockControls()
        {
            
            button4.Enabled = true;
            listBox1.Enabled = true;
            textBox_google_drive_link.Enabled = true;

            //GalaxyRP (Alex): Don't enable any of the google drive buttons if a valid google drive link was not provided.
            if (currentConfiguration.googleDriveLink != "" && currentConfiguration.googleDriveLink.Length > 34)
            {
                button1.Enabled = true;

                //GalaxyRP (Alex): Only enable download button if something was actually selected.
                if (listBox1.SelectedIndex != -1)
                {
                    button2.Enabled = true;
                }
                //GalaxyRP (Alex): Only enable download all button if there's something to download.
                if (listBox1.Items.Count != 0)
                {
                    button5.Enabled = true;
                }
            }
        }

        //GalaxyRP (Alex): Reports on the progress of the current download.
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
                        progressReporter.Report(progressValue);
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

        //GalaxyRP (Alex): Updates the file details window by looking up a fileIndex in files.
        private void UpdateFileDetails(int fileIndex)
        {
            label_filename.Text = files[fileIndex].Name;
            label_filesize.Text = (files[fileIndex].Size / 1000000).ToString() + "MB";
            label_version_number.Text = files[fileIndex].Version.ToString();
            label_author.Text = files[fileIndex].Owners[0].DisplayName;
            label_last_changed.Text = files[fileIndex].ModifiedTime.ToString();
        }

        //GalaxyRP (Alex): Things that happen once a user selects another item in listbox1
        private void listBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            UpdateFileDetails(listBox1.SelectedIndex);

            UnlockControls();
        }

        private void button3_Click(object sender, EventArgs e)
        {
            if (!Regex.IsMatch(textBox_resolution_x.Text, "^[0-9]*$") || !Regex.IsMatch(textBox_resolution_y.Text, "^[0-9]*$"))
            {
                MessageBox.Show("Resolution fields MUST be numbers.", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            else
            {
                saveConfig();
                RefreshControls();
            }
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

        private void button4_Click(object sender, EventArgs e)
        {
            ProcessStartInfo startInfo = new ProcessStartInfo();

            startInfo.Arguments = "";

            if (currentConfiguration.resolution_x != 0 && currentConfiguration.resolution_x != 0)
            {
                startInfo.Arguments += " +set r_customwidth " + currentConfiguration.resolution_x + " +set r_customheight " + currentConfiguration.resolution_y + " ";
            }

            int selectedServerIndex = comboBox_server_selection.SelectedIndex;

            if(selectedServerIndex == 0)
            {
                if(currentConfiguration.serverIP != "")
                {
                    startInfo.Arguments += " +connect " + currentConfiguration.serverIP + " ";
                }
            }
            else
            {
                if (currentConfiguration.serverIP2 != "")
                {
                    startInfo.Arguments += " +connect " + currentConfiguration.serverIP2 + " ";
                }
            }

            if (currentConfiguration.otherArguments != "")
            {
                startInfo.Arguments += currentConfiguration.otherArguments + " ";
            }

            switch (currentConfiguration.clientMod)
            {
                case "OpenJK":
                    startInfo.FileName = "openjk.x86.exe";
                    break;
                case "BaseJKA":
                default:
                    startInfo.FileName = "jamp.exe";
                    break;
            }

            Process.Start(startInfo);
        }

    }
    public class LauncherConfig
    {
        //GalaxyRP (Alex): Full link that the user imputs in the settings tab. Needs to be trimmed before it's useable.
        public string googleDriveLink { get; set; }
        public int resolution_x { get; set; }
        public int resolution_y { get; set; }
        //GalaxyRP (Alex): Client mod to use when running the game. BaseJKA and OpenJK are so far the only valid options.
        public string clientMod { get; set; }
        public string serverIP { get; set; }
        public string serverName { get; set; }
        public string serverIP2 { get; set; }
        public string server2Name { get; set; }
        //GalaxyRP (Alex): Extra arguments to run with the game.
        public string otherArguments { get; set; }
    }
}
