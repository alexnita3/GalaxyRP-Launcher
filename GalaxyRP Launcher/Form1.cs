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

namespace GalaxyRP_Launcher
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
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
                    "user", CancellationToken.None, new FileDataStore("Drive.ListMyFiles"));
            }

            DriveService service = new DriveService(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "GalaxyRP Launcher",
            });

            return service;
        }

        private async Task<FileList> GetFileList()
        {
            DriveService service = await CreateService();

            var testRequest = service.Files.List();

            FileList filelist = testRequest.Execute();

            foreach(Google.Apis.Drive.v3.Data.File file in filelist.Files)
            {
                listBox1.Items.Add(file.Name);
            }
            return filelist;
        }
    }
}
