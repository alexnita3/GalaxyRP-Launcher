﻿using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;

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
using System.IO;
using System.Threading;
using System.Linq;

namespace GalaxyRP_Launcher
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {

        //GalaxyRP (Alex): Location of the base folder.
        string filepath = System.IO.Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location) + "\\base";
        //GalaxyRP (Alex): This variable will store all the file metadata we get from Google drive.
        public IList<Google.Apis.Drive.v3.Data.File> files;
        //GalaxyRP (Alex): This will store the filenames of files that will be removed, if the option is active.
        public List<String> filesToBeDeleted;
        //GalaxyRP (Alex): Id of the drive folder we'll be interacting with.
        public string googleDriveFolderId;
        public string deletionIndicator = "🗑️";
        public string downloadIndicator = "✅";

        LauncherConfig currentConfiguration = new LauncherConfig();

        public List<string> googleDriveSubfolderIds = new List<string>();

        public MainWindow()
        {
            AppDomain.CurrentDomain.AssemblyResolve += (sender, args) =>
            {
                string resourceName = new AssemblyName(args.Name).Name + ".dll";
                string resource = Array.Find(this.GetType().Assembly.GetManifestResourceNames(), element => element.EndsWith(resourceName));

                using (var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream(resource))
                {
                    Byte[] assemblyData = new Byte[stream.Length];
                    stream.Read(assemblyData, 0, assemblyData.Length);
                    return Assembly.Load(assemblyData);
                }
            };

            InitializeComponent();

#if !DEBUG
            if (!detectJKA())
            {
                System.Environment.Exit(1);
            }
#endif

            if (!File.Exists("launcher_config.cfg"))
            {
                buildDefaultConfig();

            }
            string json = System.IO.File.ReadAllText("launcher_config.cfg");
            GetSettingsFromConfig(json);

            label_filename.Content = "";
            label_filesize.Content = "";
            label_version_number.Content = "";
            label_last_changed.Content = "";

            button6.IsEnabled = false;
            button6.Visibility = Visibility.Hidden;

            RefreshControls();

            reset_task_status_label();

            if(currentConfiguration.removeExtraFiles == true)
            {
                button5.Content = "Sync All";
            }

            if(currentConfiguration.scanAutomatically == true && is_valid_drive_link(currentConfiguration.googleDriveLink))
            {
                begin_search();
            }
        }

        void reset_task_status_label()
        {
            label_task_status.Content = "Nothing to do. Click on 'Check Updates' to start syncing your pk3s.";
        }

        void update_task_status_downloading(int percentage)
        {
            if (percentage != 100)
            {
                progressBar1.IsIndeterminate = false;
                label_task_status.Content = "Downloading..." + percentage.ToString() + "%";
            }
            else
            {
                progressBar1.IsIndeterminate = true;
                label_task_status.Content = "File Downloaded...Writing to disk.";
            }
        }

        void update_task_status_comparing()
        {
            label_task_status.Content = "Comparing your files to Google Drive, please be patient...";
            this.progressBar1.IsIndeterminate = true;
        }

        void update_task_status_select_file_instruction()
        {
            label_task_status.Content = "Select a file and click 'Download Selected', or simply click 'Download All'.";
            this.progressBar1.IsIndeterminate = false;
        }

        void update_task_status_launch_game_instruction()
        {
            label_task_status.Content = "Everything is up to date! You can launch your game safely.";
        }

        Boolean detectJKA()
        {

            if (!Directory.Exists(filepath))
            {
                MessageBox.Show("Base folder was not detected. Please install the application in your GameData folder.", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                return false;
            }

            if (!File.Exists("jamp.exe"))
            {
                MessageBox.Show("jamp.exe was not detected. Please install the application in your GameData folder.", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                return false;
            }

            if (!filepath.Contains("GameData"))
            {
                MessageBox.Show("Application is not in the GameData folder. Please install the application in your GameData folder.", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                return false;
            }

            return true;
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
            currentConfiguration.scanAutomatically = false;
            currentConfiguration.downloadAutomatically = false;

            string json = JsonConvert.SerializeObject(currentConfiguration);
            //write string to file
            System.IO.File.WriteAllText("launcher_config.cfg", json);
        }

        List<String> getCompleteHashList()
        {
            List<String> hashList = new List<String>();
            //GalaxyRP (Alex): Process the list of files found in the directory.
            string[] fileEntries = Directory.GetFiles(filepath);

            foreach (string fileName in fileEntries)
            {
                var md5 = System.Security.Cryptography.MD5.Create();
                var stream = File.OpenRead(fileName);

                var hash = md5.ComputeHash(stream);
                //GalaxyRP (Alex): Format the string so that it matches the google drive API's
                var md5String = BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();

                hashList.Add(md5String);

                stream.Close();
            }

            return hashList;

        }

        List<String> getFilesToBeDeleted(IList<Google.Apis.Drive.v3.Data.File> googleDriveCompleteFileList)
        {

            List<String> filesToBeIgnored = new List<string> { "assets0.pk3", "assets1.pk3", "assets2.pk3", "assets3.pk3" };

            List<String> filesOnHardDrive = Directory.GetFiles(filepath).ToList<String>();
            List<String> googleDriveFileNames = new List<String>();
            List<String> filesToDelete = new List<String>();

            foreach (Google.Apis.Drive.v3.Data.File driveFile in googleDriveCompleteFileList)
            {
                googleDriveFileNames.Add(driveFile.Name);
            }

            for (int i = 0;i < filesOnHardDrive.Count; i++)
            {
                filesOnHardDrive[i] = filesOnHardDrive[i].Replace(filepath + "\\", "");
            }

            filesToDelete = filesOnHardDrive.Except(googleDriveFileNames).ToList();

            filesToDelete = filesToDelete.Except(filesToBeIgnored).ToList();

            for (int i = 0; i < filesToDelete.Count; i++)
            {
                if (!filesToDelete[i].Contains(".pk3")) 
                {
                    filesToDelete.RemoveAt(i);
                    i--;
                }
            }

            return filesToDelete;
        }

        //GalaxyRP (Alex): Checks the checksum of each file against the ones in the cloud. (The ones stored in files that were grabbed via the api)
        void compareLocalFilesWithCloud()
        {
            List<String> hashList = getCompleteHashList();

            for (int i = 0; i < files.Count; i++)
            {
                Google.Apis.Drive.v3.Data.File file = files[i];

                //GalaxyRP (Alex): Compare the checksum of the files in the drive with the pre-processed list of existing files' checksums.
                if (hashList.Contains(file.Md5Checksum))
                {
                    //GalaxyRP (Alex): If user already has it, remove it form the list.
                    files.RemoveAt(i);
                    i--;
                }
            }
        }

        void initializeClientModComboBox(string clientMod)
        {
            for(int i = 0; i < comboBox_client_mod.Items.Count; i++)
            {
                if(((ContentControl)comboBox_client_mod.Items[i]).Content.ToString() == clientMod)
                {
                    comboBox_client_mod.SelectedIndex = i;
                    return;
                }
            }

            comboBox_client_mod.SelectedIndex = 0;
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
            initializeClientModComboBox(currentConfiguration.clientMod);
            checkBox_scan_automatically.IsChecked = currentConfiguration.scanAutomatically;
            checkBox_download_automatically.IsChecked = currentConfiguration.downloadAutomatically;
            checkBox_delete_extra_files.IsChecked = currentConfiguration.removeExtraFiles;

            textBox_google_drive_link.Text = currentConfiguration.googleDriveLink;
            if (is_valid_drive_link(currentConfiguration.googleDriveLink))
            {
                googleDriveFolderId = GetGoogleDriveFolderIdFromString(currentConfiguration.googleDriveLink)[0].Value;
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
            currentConfiguration.scanAutomatically = (Boolean)checkBox_scan_automatically.IsChecked;
            currentConfiguration.downloadAutomatically = (Boolean)checkBox_download_automatically.IsChecked;
            currentConfiguration.removeExtraFiles = (Boolean)checkBox_delete_extra_files.IsChecked;

            string json = JsonConvert.SerializeObject(currentConfiguration, Formatting.Indented);
            //write string to file
            System.IO.File.WriteAllText("launcher_config.cfg", json);
            GetSettingsFromConfig(json);
        }

        //GalaxyRP (Alex): Return the Id of the file that's currently selected in listBox1
        private string GetCurrentSelectedFileId()
        {
            return files[listBox1.SelectedIndex].Id;
        }

        private void RefreshPk3UiList()
        {
            listBox1.Items.Clear();

            foreach (Google.Apis.Drive.v3.Data.File file in files)
            {
                listBox1.Items.Add(downloadIndicator + file.Name);

            }

            if (currentConfiguration.removeExtraFiles == true)
            {
                foreach (String fileToBeDeleted in filesToBeDeleted)
                {
                    listBox1.Items.Add(deletionIndicator + fileToBeDeleted);
                }
            }

            label_number_of_files_to_download.Content = files.Count.ToString();
            if (currentConfiguration.removeExtraFiles == true)
            {
                label_number_of_files_to_delete.Content = filesToBeDeleted.Count.ToString();
            }
        }

        //GalaxyRP (Alex): Connects to Google Drive via the api key.
        private async Task<DriveService> CreateService()
        {
            UserCredential credential;
#if DEBUG
            using (var stream = new FileStream("client_secrets.json", FileMode.Open, FileAccess.Read))
            {

                credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(
                    GoogleClientSecrets.FromStream(stream).Secrets,
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

        //GalaxyRP (Alex): Filters the response form Google drive, and makes sure we only get pk3 files.
        IList<Google.Apis.Drive.v3.Data.File> FilterFileList(IList<Google.Apis.Drive.v3.Data.File> originalFileList)
        {
            int i = 0;
            while (i < originalFileList.Count)
            {
                Boolean changed = false;

                if (originalFileList[i].Trashed != false)
                {
                    originalFileList.RemoveAt(i);
                    changed = true;
                }
                else if (originalFileList[i].FullFileExtension == null)
                {
                    originalFileList.RemoveAt(i);
                    changed = true;
                }
                else if (!originalFileList[i].FullFileExtension.Contains("pk3"))
                {
                    originalFileList.RemoveAt(i);
                    changed = true;
                }

                if (!changed)
                {
                    i++;
                }
            }

            return originalFileList;
        }

        //GalaxyRP (Alex): Searches google drive for all files, then applies a filter on those. Also fills in listBox1 with values.
        private async Task<IList<Google.Apis.Drive.v3.Data.File>> GetFileList()
        {
            DriveService service = await CreateService();

            var testRequest = service.Files.List();
            testRequest.SupportsAllDrives = true;
            testRequest.SupportsTeamDrives = true;
            testRequest.IncludeItemsFromAllDrives = true;
            testRequest.Q = "parents in '" + googleDriveFolderId + "'";
            testRequest.PageSize = 1000;
            testRequest.Fields = "*";

            foreach (string subfolderId in googleDriveSubfolderIds)
            {
                testRequest.Q += " or parents in '" + subfolderId + "'";
            }

            try
            {
                FileList filelist = testRequest.Execute();
                testRequest.PageToken = filelist.NextPageToken;
                files = filelist.Files;
                while (testRequest.PageToken != null)
                {
                    filelist = testRequest.Execute();

                    foreach (Google.Apis.Drive.v3.Data.File file in filelist.Files)
                    {
                        files.Add(file);
                    }
                    testRequest.PageToken = filelist.NextPageToken;
                }
            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message, "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }


            files = FilterFileList(files);

            if (currentConfiguration.removeExtraFiles == true)
            {
                filesToBeDeleted = getFilesToBeDeleted(files);
            }

            compareLocalFilesWithCloud();

            return files;
        }

        //GalaxyRP (Alex): Makes a request for all subfolders inside the main folder and populates googleDriveSubfolderIds with the Ids.
        private async Task GetSubfolderList(string parentFolderId)
        {
            DriveService service = await CreateService();

            var testRequest = service.Files.List();
            testRequest.SupportsAllDrives = true;
            testRequest.IncludeItemsFromAllDrives = true;
            testRequest.Q = "mimeType='application/vnd.google-apps.folder' and parents in '" + parentFolderId + "'";
            testRequest.Fields = "*";

            try
            {
                FileList filelist = testRequest.Execute();
                files = filelist.Files;
            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message, "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }


            foreach (Google.Apis.Drive.v3.Data.File file in files)
            {
                await GetSubfolderList(file.Id);
                googleDriveSubfolderIds.Add(file.Id);
            }
        }

        //GalaxyRP (Alex): Given an Id, search files for the size in bytes of the item with that id.
        private long GetSizeOfItemWithId(string fileId)
        {
            for (int i = 0; i < files.Count; i++)
            {
                if (files[i].Id == fileId)
                {
                    return (long)files[i].Size;
                }
            }
            return 0;
        }

        //GalaxyRP (Alex): ASYNCHRONOUS Given a file id, starts to download it from Google Drive.
        private async Task StartDownloadAsync(string fileId)
        {
            progressBar1.Visibility = Visibility.Visible;

            DriveService service = await CreateService();

            progressBar1.Minimum = 0;
            progressBar1.Maximum = 100;
            progressBar1.Value = 0;

            //GalaxyRP (Alex): Creating an instance of Progress<T> captures the current 
            //GalaxyRP (Alex): SynchronizationContext (UI context) to prevent cross threading when updating the ProgressBar
            IProgress<double> progressReporter =
              new Progress<double>(value => {
                  progressBar1.Value = (int)value;
                  update_task_status_downloading((int)value);
              });

            await DownloadAsync(progressReporter, fileId);

            await Task.Run(async () =>
            {
                await GetFileList();
            });

            progressBar1.Visibility = Visibility.Hidden;

            RefreshPk3UiList();

        }

        //GalaxyRP (Alex): Manages the ongoing download process.
        private async Task DownloadAsync(IProgress<double> progressReporter, string fileId)
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
            button1.IsEnabled = false;
            button2.IsEnabled = false;
            button4.IsEnabled = false;
            button5.IsEnabled = false;
            listBox1.IsEnabled = false;
            textBox_google_drive_link.IsEnabled = false;
        }


        MatchCollection GetGoogleDriveFolderIdFromString(string fullString)
        {
            string pattern = @"[-\w]{25,}";
            MatchCollection matches;

            Regex defaultRegex = new Regex(pattern);
            matches = defaultRegex.Matches(fullString);

            return matches;
        }

        Boolean is_valid_drive_link(string link)
        {
            MatchCollection matches = GetGoogleDriveFolderIdFromString(link);

            if (matches.Count > 0 && matches[0].Value == link)
            {
                return true;
            }
            return false;
        }

        //GalaxyRP (Alex): Unlocks important buttons. Also takes care to not unlock buttons that require other actions to be performed first.
        void UnlockControls()
        {

            button4.IsEnabled = true;
            listBox1.IsEnabled = true;
            textBox_google_drive_link.IsEnabled = true;

            //GalaxyRP (Alex): Don't enable any of the google drive buttons if a valid google drive link was not provided.
            if (is_valid_drive_link(currentConfiguration.googleDriveLink))
            {
                button1.IsEnabled = true;

                //GalaxyRP (Alex): Only enable download button if something was actually selected.
                if (listBox1.SelectedIndex != -1)
                {
                    button2.IsEnabled = true;
                }
                //GalaxyRP (Alex): Only enable download all button if there's something to download.
                if (listBox1.Items.Count != 0)
                {
                    button5.IsEnabled = true;
                }
            }

            if (listBox1.Items.Count > 0)
            {
                update_task_status_select_file_instruction();
            }
            else
            {
                update_task_status_launch_game_instruction();
                button2.IsEnabled = false;
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
                        if (progressValue > 100)
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

        private Boolean isFileToBeDownloaded()
        {
            if (listBox1.SelectedValue.ToString().Contains(downloadIndicator))
            {
                return true;
            }
            return false;
        }

        private Boolean isFileToBeDeleted()
        {
            if (listBox1.SelectedValue.ToString().Contains(deletionIndicator))
            {
                return true;
            }
            return false;
        }

        //GalaxyRP (Alex): Updates the file details window by looking up a fileIndex in files.
        private void UpdateFileDetails(int fileIndex)
        {
            label_filename.Content = files[fileIndex].Name;
            label_filesize.Content = (files[fileIndex].Size / 1000000).ToString() + "MB";
            label_version_number.Content = files[fileIndex].Version.ToString();
            label_last_changed.Content = files[fileIndex].ModifiedTime.ToString();

            
        }
        
        private void UpdateFileToBeDeleted(string filename)
        {
            label_filename.Content = filename;
            label_filesize.Content = "";
            label_version_number.Content = "";
            label_last_changed.Content = "";

            button2.IsEnabled = false;
        }

        private void DeleteFilesNotOnDrive()
        {
            foreach (String fileToBeDeleted in filesToBeDeleted)
            {
                File.Delete(Path.Combine(filepath, fileToBeDeleted));
            }
        }

        private async void begin_search()
        {
            progressBar1.Visibility = Visibility.Visible;
            LockControls();
            update_task_status_comparing();
            await Task.Run(async () =>
            {
                await GetSubfolderList(googleDriveFolderId);
                await GetFileList();
            });

            RefreshPk3UiList();

            progressBar1.Visibility = Visibility.Hidden;

            UnlockControls();

            if (currentConfiguration.downloadAutomatically == true)
            {
                button2.IsEnabled = false;
                download_all();
            }
        }

        private void button1_Click_1(object sender, RoutedEventArgs e)
        {
            begin_search();
        }

        private async void button2_Click_1(object sender, RoutedEventArgs e)
        {
            string selectedFileId = GetCurrentSelectedFileId();
            LockControls();
            if (currentConfiguration.removeExtraFiles == true)
            {
                DeleteFilesNotOnDrive();
            }
            await StartDownloadAsync(selectedFileId);
            UnlockControls();
        }

        //GalaxyRP (Alex): Things that happen once a user selects another item in listbox1
        private void listBox1_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (listBox1.Items.Count == 0)
                return;

            if (isFileToBeDownloaded())
            {
                UpdateFileDetails(listBox1.SelectedIndex);

                button2.IsEnabled = true;
                button2.Visibility = Visibility.Visible;

                button6.IsEnabled = false;
                button6.Visibility = Visibility.Hidden;
            }

            UnlockControls();

            if (isFileToBeDeleted())
            {
                UpdateFileToBeDeleted(listBox1.SelectedValue.ToString().Replace(deletionIndicator, ""));
                
                button2.IsEnabled = false;
                button2.Visibility = Visibility.Hidden;

                button6.IsEnabled = true;
                button6.Visibility = Visibility.Visible;
            }
        }

        private async void download_all()
        {
            LockControls();

            while (listBox1.Items.Count != 0)
            {
                UpdateFileDetails(0);
                await StartDownloadAsync(files[0].Id);

            }
            UnlockControls();
        }

        private void button5_Click_1(object sender, RoutedEventArgs e)
        {
            if (currentConfiguration.removeExtraFiles == true)
            {
                DeleteFilesNotOnDrive();
            }
            button2.IsEnabled = false;
            download_all();
        }

        private void button4_Click_1(object sender, RoutedEventArgs e)
        {
            ProcessStartInfo startInfo = new ProcessStartInfo();

            startInfo.Arguments = "";

            if (currentConfiguration.resolution_x != 0 && currentConfiguration.resolution_x != 0)
            {
                startInfo.Arguments += " +set r_customwidth " + currentConfiguration.resolution_x + " +set r_customheight " + currentConfiguration.resolution_y + " ";
            }

            int selectedServerIndex = comboBox_server_selection.SelectedIndex;

            if (selectedServerIndex == 0)
            {
                if (currentConfiguration.serverIP != "")
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
                case "EternalJK":
                    startInfo.FileName = "eternaljk.x86.exe";
                    break;
                case "Base JKA":
                default:
                    startInfo.FileName = "jamp.exe";
                    break;
            }
            try
            {
                Process.Start(startInfo);
            }
            catch (Exception exception)
            {
                MessageBox.Show(exception.Message, "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void button3_Click_1(object sender, RoutedEventArgs e)
        {
            if (!Regex.IsMatch(textBox_resolution_x.Text, "^[0-9]*$") || !Regex.IsMatch(textBox_resolution_y.Text, "^[0-9]*$"))
            {
                MessageBox.Show("Resolution fields MUST be numbers.", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
            else if (!is_valid_drive_link(textBox_google_drive_link.Text))
            {
                MessageBox.Show("The Google Drive link provided is in the wrong format. An example of a valid value is 1Rw36z4WWnKRLwYkunzRRzuffrX1WJy8G .", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
            else
            {
                saveConfig();
                RefreshControls();
                reset_task_status_label();
                MessageBox.Show("The configuration changes were saved!", "Info", MessageBoxButton.OK, MessageBoxImage.Information);
            }
        }

        private void button6_Click(object sender, RoutedEventArgs e)
        {
            string filename = listBox1.SelectedValue.ToString().Replace(deletionIndicator, "");

            File.Delete(Path.Combine(filepath, filename));
            filesToBeDeleted.Remove(filename);
            RefreshPk3UiList();
        }
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
        public Boolean scanAutomatically { get; set; }
        public Boolean downloadAutomatically { get; set; }
        public Boolean removeExtraFiles { get; set; }
}


