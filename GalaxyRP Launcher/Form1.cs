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

namespace GalaxyRP_Launcher
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        IList<Google.Apis.Drive.v3.Data.File> ProcessFileList(IList<Google.Apis.Drive.v3.Data.File> originalFileList)
        {

            for(int i = 0; i < originalFileList.Count; i++)
            {
                if (!originalFileList[i].Name.Contains(".pk3"))
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

        IList<Google.Apis.Drive.v3.Data.File> files;

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
            testRequest.Q = "parents in '1krZva8NV7BBDsivrRiu0keOfQ-y_pEyS'";

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

            foreach (Google.Apis.Drive.v3.Data.File file in files)
            {
                listBox1.Items.Add(file.Name);

            }
            return files;
        }

        private async Task DownloadFile(string fileId)
        {
            DriveService service = await CreateService();

            var request = service.Files.Get(fileId);
            var stream = new MemoryStream();

            // Add a handler which will be notified on progress changes.
            // It will notify on each chunk download and when the
            // download is completed or failed.
            request.MediaDownloader.ProgressChanged +=
                progress =>
                {
                    switch (progress.Status)
                    {
                        case DownloadStatus.Downloading:
                            {
                                Console.WriteLine(progress.BytesDownloaded);
                                break;
                            }
                        case DownloadStatus.Completed:
                            {
                                Console.WriteLine("Download complete.");
                                break;
                            }
                        case DownloadStatus.Failed:
                            {
                                Console.WriteLine("Download failed.");
                                break;
                            }
                    }
                };
            request.Download(stream);

            //Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);

            string filePath = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);

            filePath += "\\" + GetCurrentSelectedFileName();

            FileStream fileStream = new FileStream(filePath, FileMode.Create, FileAccess.Write);

            stream.WriteTo(fileStream);

            fileStream.Close();

            stream.Dispose();
            stream.Close();
        }

        private void button2_Click(object sender, EventArgs e)
        {

            string selectedFileId = GetCurrentSelectedFileId();

            DownloadFile(selectedFileId);
            
        }
    }
}
