
namespace GalaxyRP_Launcher
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.listBox1 = new System.Windows.Forms.ListBox();
            this.button1 = new System.Windows.Forms.Button();
            this.button2 = new System.Windows.Forms.Button();
            this.progressBar1 = new System.Windows.Forms.ProgressBar();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.label_filesize = new System.Windows.Forms.Label();
            this.label_last_changed = new System.Windows.Forms.Label();
            this.label_version_number = new System.Windows.Forms.Label();
            this.label_filename = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label1 = new System.Windows.Forms.Label();
            this.tabControl1 = new System.Windows.Forms.TabControl();
            this.tabPage1 = new System.Windows.Forms.TabPage();
            this.button5 = new System.Windows.Forms.Button();
            this.comboBox_server_selection = new System.Windows.Forms.ComboBox();
            this.button4 = new System.Windows.Forms.Button();
            this.tabPage2 = new System.Windows.Forms.TabPage();
            this.label14 = new System.Windows.Forms.Label();
            this.textBox_other_arguments = new System.Windows.Forms.TextBox();
            this.textBox_server_name_2 = new System.Windows.Forms.TextBox();
            this.label12 = new System.Windows.Forms.Label();
            this.textBox_server_ip_2 = new System.Windows.Forms.TextBox();
            this.label13 = new System.Windows.Forms.Label();
            this.textBox_server_name = new System.Windows.Forms.TextBox();
            this.label11 = new System.Windows.Forms.Label();
            this.textBox_resolution_y = new System.Windows.Forms.TextBox();
            this.textBox_resolution_x = new System.Windows.Forms.TextBox();
            this.label10 = new System.Windows.Forms.Label();
            this.label9 = new System.Windows.Forms.Label();
            this.comboBox_client_mod = new System.Windows.Forms.ComboBox();
            this.textBox_google_drive_link = new System.Windows.Forms.TextBox();
            this.textBox_server_ip = new System.Windows.Forms.TextBox();
            this.label8 = new System.Windows.Forms.Label();
            this.label7 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.button3 = new System.Windows.Forms.Button();
            this.groupBox1.SuspendLayout();
            this.tabControl1.SuspendLayout();
            this.tabPage1.SuspendLayout();
            this.tabPage2.SuspendLayout();
            this.SuspendLayout();
            // 
            // listBox1
            // 
            this.listBox1.FormattingEnabled = true;
            this.listBox1.ItemHeight = 16;
            this.listBox1.Location = new System.Drawing.Point(3, 6);
            this.listBox1.Name = "listBox1";
            this.listBox1.Size = new System.Drawing.Size(254, 276);
            this.listBox1.TabIndex = 0;
            this.listBox1.SelectedIndexChanged += new System.EventHandler(this.listBox1_SelectedIndexChanged);
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(266, 141);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(138, 32);
            this.button1.TabIndex = 1;
            this.button1.Text = "Check Updates";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // button2
            // 
            this.button2.Enabled = false;
            this.button2.Location = new System.Drawing.Point(445, 141);
            this.button2.Name = "button2";
            this.button2.Size = new System.Drawing.Size(157, 32);
            this.button2.TabIndex = 2;
            this.button2.Text = "Download Selected";
            this.button2.UseVisualStyleBackColor = true;
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // progressBar1
            // 
            this.progressBar1.Location = new System.Drawing.Point(263, 195);
            this.progressBar1.Name = "progressBar1";
            this.progressBar1.Size = new System.Drawing.Size(516, 26);
            this.progressBar1.TabIndex = 3;
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.label_filesize);
            this.groupBox1.Controls.Add(this.label_last_changed);
            this.groupBox1.Controls.Add(this.label_version_number);
            this.groupBox1.Controls.Add(this.label_filename);
            this.groupBox1.Controls.Add(this.label5);
            this.groupBox1.Controls.Add(this.label3);
            this.groupBox1.Controls.Add(this.label2);
            this.groupBox1.Controls.Add(this.label1);
            this.groupBox1.Location = new System.Drawing.Point(266, 6);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(516, 129);
            this.groupBox1.TabIndex = 4;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "File Details";
            // 
            // label_filesize
            // 
            this.label_filesize.AutoSize = true;
            this.label_filesize.Location = new System.Drawing.Point(117, 46);
            this.label_filesize.Name = "label_filesize";
            this.label_filesize.Size = new System.Drawing.Size(46, 17);
            this.label_filesize.TabIndex = 9;
            this.label_filesize.Text = "label9";
            // 
            // label_last_changed
            // 
            this.label_last_changed.AutoSize = true;
            this.label_last_changed.Location = new System.Drawing.Point(117, 99);
            this.label_last_changed.Name = "label_last_changed";
            this.label_last_changed.Size = new System.Drawing.Size(46, 17);
            this.label_last_changed.TabIndex = 8;
            this.label_last_changed.Text = "label9";
            // 
            // label_version_number
            // 
            this.label_version_number.AutoSize = true;
            this.label_version_number.Location = new System.Drawing.Point(117, 73);
            this.label_version_number.Name = "label_version_number";
            this.label_version_number.Size = new System.Drawing.Size(46, 17);
            this.label_version_number.TabIndex = 6;
            this.label_version_number.Text = "label7";
            // 
            // label_filename
            // 
            this.label_filename.AutoSize = true;
            this.label_filename.Location = new System.Drawing.Point(117, 18);
            this.label_filename.Name = "label_filename";
            this.label_filename.Size = new System.Drawing.Size(46, 17);
            this.label_filename.TabIndex = 5;
            this.label_filename.Text = "label6";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(7, 99);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(100, 17);
            this.label5.TabIndex = 4;
            this.label5.Text = "Last Changed:";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(6, 73);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(114, 17);
            this.label3.TabIndex = 2;
            this.label3.Text = "Version Number:";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(7, 46);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(39, 17);
            this.label2.TabIndex = 1;
            this.label2.Text = "Size:";
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(6, 18);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(75, 17);
            this.label1.TabIndex = 0;
            this.label1.Text = "File Name:";
            // 
            // tabControl1
            // 
            this.tabControl1.Controls.Add(this.tabPage1);
            this.tabControl1.Controls.Add(this.tabPage2);
            this.tabControl1.Location = new System.Drawing.Point(12, 10);
            this.tabControl1.Name = "tabControl1";
            this.tabControl1.SelectedIndex = 0;
            this.tabControl1.Size = new System.Drawing.Size(796, 327);
            this.tabControl1.TabIndex = 5;
            // 
            // tabPage1
            // 
            this.tabPage1.Controls.Add(this.button5);
            this.tabPage1.Controls.Add(this.comboBox_server_selection);
            this.tabPage1.Controls.Add(this.button4);
            this.tabPage1.Controls.Add(this.listBox1);
            this.tabPage1.Controls.Add(this.progressBar1);
            this.tabPage1.Controls.Add(this.groupBox1);
            this.tabPage1.Controls.Add(this.button2);
            this.tabPage1.Controls.Add(this.button1);
            this.tabPage1.Location = new System.Drawing.Point(4, 25);
            this.tabPage1.Name = "tabPage1";
            this.tabPage1.Padding = new System.Windows.Forms.Padding(3);
            this.tabPage1.Size = new System.Drawing.Size(788, 298);
            this.tabPage1.TabIndex = 0;
            this.tabPage1.Text = "tabPage1";
            this.tabPage1.UseVisualStyleBackColor = true;
            // 
            // button5
            // 
            this.button5.Enabled = false;
            this.button5.Location = new System.Drawing.Point(641, 141);
            this.button5.Name = "button5";
            this.button5.Size = new System.Drawing.Size(138, 32);
            this.button5.TabIndex = 7;
            this.button5.Text = "Download All";
            this.button5.UseVisualStyleBackColor = true;
            this.button5.Click += new System.EventHandler(this.button5_Click);
            // 
            // comboBox_server_selection
            // 
            this.comboBox_server_selection.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboBox_server_selection.FormattingEnabled = true;
            this.comboBox_server_selection.Location = new System.Drawing.Point(454, 231);
            this.comboBox_server_selection.Name = "comboBox_server_selection";
            this.comboBox_server_selection.Size = new System.Drawing.Size(325, 24);
            this.comboBox_server_selection.TabIndex = 6;
            // 
            // button4
            // 
            this.button4.Location = new System.Drawing.Point(263, 231);
            this.button4.Name = "button4";
            this.button4.Size = new System.Drawing.Size(185, 51);
            this.button4.TabIndex = 5;
            this.button4.Text = "Launch Game";
            this.button4.UseVisualStyleBackColor = true;
            this.button4.Click += new System.EventHandler(this.button4_Click);
            // 
            // tabPage2
            // 
            this.tabPage2.Controls.Add(this.label14);
            this.tabPage2.Controls.Add(this.textBox_other_arguments);
            this.tabPage2.Controls.Add(this.textBox_server_name_2);
            this.tabPage2.Controls.Add(this.label12);
            this.tabPage2.Controls.Add(this.textBox_server_ip_2);
            this.tabPage2.Controls.Add(this.label13);
            this.tabPage2.Controls.Add(this.textBox_server_name);
            this.tabPage2.Controls.Add(this.label11);
            this.tabPage2.Controls.Add(this.textBox_resolution_y);
            this.tabPage2.Controls.Add(this.textBox_resolution_x);
            this.tabPage2.Controls.Add(this.label10);
            this.tabPage2.Controls.Add(this.label9);
            this.tabPage2.Controls.Add(this.comboBox_client_mod);
            this.tabPage2.Controls.Add(this.textBox_google_drive_link);
            this.tabPage2.Controls.Add(this.textBox_server_ip);
            this.tabPage2.Controls.Add(this.label8);
            this.tabPage2.Controls.Add(this.label7);
            this.tabPage2.Controls.Add(this.label6);
            this.tabPage2.Controls.Add(this.button3);
            this.tabPage2.Location = new System.Drawing.Point(4, 25);
            this.tabPage2.Name = "tabPage2";
            this.tabPage2.Padding = new System.Windows.Forms.Padding(3);
            this.tabPage2.Size = new System.Drawing.Size(788, 298);
            this.tabPage2.TabIndex = 1;
            this.tabPage2.Text = "tabPage2";
            this.tabPage2.UseVisualStyleBackColor = true;
            // 
            // label14
            // 
            this.label14.AutoSize = true;
            this.label14.Location = new System.Drawing.Point(6, 211);
            this.label14.Name = "label14";
            this.label14.Size = new System.Drawing.Size(131, 17);
            this.label14.TabIndex = 18;
            this.label14.Text = "Custom Arguments:";
            // 
            // textBox_other_arguments
            // 
            this.textBox_other_arguments.Location = new System.Drawing.Point(139, 208);
            this.textBox_other_arguments.Name = "textBox_other_arguments";
            this.textBox_other_arguments.Size = new System.Drawing.Size(643, 22);
            this.textBox_other_arguments.TabIndex = 17;
            // 
            // textBox_server_name_2
            // 
            this.textBox_server_name_2.Location = new System.Drawing.Point(139, 91);
            this.textBox_server_name_2.Name = "textBox_server_name_2";
            this.textBox_server_name_2.Size = new System.Drawing.Size(643, 22);
            this.textBox_server_name_2.TabIndex = 16;
            // 
            // label12
            // 
            this.label12.AutoSize = true;
            this.label12.Location = new System.Drawing.Point(6, 94);
            this.label12.Name = "label12";
            this.label12.Size = new System.Drawing.Size(107, 17);
            this.label12.TabIndex = 15;
            this.label12.Text = "Server 2 Name:";
            // 
            // textBox_server_ip_2
            // 
            this.textBox_server_ip_2.Location = new System.Drawing.Point(139, 63);
            this.textBox_server_ip_2.Name = "textBox_server_ip_2";
            this.textBox_server_ip_2.Size = new System.Drawing.Size(643, 22);
            this.textBox_server_ip_2.TabIndex = 14;
            // 
            // label13
            // 
            this.label13.AutoSize = true;
            this.label13.Location = new System.Drawing.Point(6, 66);
            this.label13.Name = "label13";
            this.label13.Size = new System.Drawing.Size(82, 17);
            this.label13.TabIndex = 13;
            this.label13.Text = "Server 2 IP:";
            // 
            // textBox_server_name
            // 
            this.textBox_server_name.Location = new System.Drawing.Point(139, 35);
            this.textBox_server_name.Name = "textBox_server_name";
            this.textBox_server_name.Size = new System.Drawing.Size(643, 22);
            this.textBox_server_name.TabIndex = 12;
            // 
            // label11
            // 
            this.label11.AutoSize = true;
            this.label11.Location = new System.Drawing.Point(6, 38);
            this.label11.Name = "label11";
            this.label11.Size = new System.Drawing.Size(95, 17);
            this.label11.TabIndex = 11;
            this.label11.Text = "Server Name:";
            // 
            // textBox_resolution_y
            // 
            this.textBox_resolution_y.Location = new System.Drawing.Point(235, 180);
            this.textBox_resolution_y.Name = "textBox_resolution_y";
            this.textBox_resolution_y.Size = new System.Drawing.Size(67, 22);
            this.textBox_resolution_y.TabIndex = 10;
            // 
            // textBox_resolution_x
            // 
            this.textBox_resolution_x.Location = new System.Drawing.Point(139, 180);
            this.textBox_resolution_x.Name = "textBox_resolution_x";
            this.textBox_resolution_x.Size = new System.Drawing.Size(67, 22);
            this.textBox_resolution_x.TabIndex = 9;
            // 
            // label10
            // 
            this.label10.AutoSize = true;
            this.label10.Location = new System.Drawing.Point(212, 183);
            this.label10.Name = "label10";
            this.label10.Size = new System.Drawing.Size(17, 17);
            this.label10.TabIndex = 8;
            this.label10.Text = "X";
            // 
            // label9
            // 
            this.label9.AutoSize = true;
            this.label9.Location = new System.Drawing.Point(6, 183);
            this.label9.Name = "label9";
            this.label9.Size = new System.Drawing.Size(79, 17);
            this.label9.TabIndex = 7;
            this.label9.Text = "Resolution:";
            // 
            // comboBox_client_mod
            // 
            this.comboBox_client_mod.FormattingEnabled = true;
            this.comboBox_client_mod.Items.AddRange(new object[] {
            "BaseJKA",
            "OpenJK",
            "EternalJK (Experimental)"});
            this.comboBox_client_mod.Location = new System.Drawing.Point(139, 149);
            this.comboBox_client_mod.Name = "comboBox_client_mod";
            this.comboBox_client_mod.Size = new System.Drawing.Size(121, 24);
            this.comboBox_client_mod.TabIndex = 6;
            // 
            // textBox_google_drive_link
            // 
            this.textBox_google_drive_link.Location = new System.Drawing.Point(139, 120);
            this.textBox_google_drive_link.Name = "textBox_google_drive_link";
            this.textBox_google_drive_link.Size = new System.Drawing.Size(643, 22);
            this.textBox_google_drive_link.TabIndex = 5;
            // 
            // textBox_server_ip
            // 
            this.textBox_server_ip.Location = new System.Drawing.Point(139, 7);
            this.textBox_server_ip.Name = "textBox_server_ip";
            this.textBox_server_ip.Size = new System.Drawing.Size(643, 22);
            this.textBox_server_ip.TabIndex = 4;
            // 
            // label8
            // 
            this.label8.AutoSize = true;
            this.label8.Location = new System.Drawing.Point(6, 152);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(78, 17);
            this.label8.TabIndex = 3;
            this.label8.Text = "Client mod:";
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(6, 123);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(125, 17);
            this.label7.TabIndex = 2;
            this.label7.Text = "Google Drive Link:";
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(6, 10);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(70, 17);
            this.label6.TabIndex = 1;
            this.label6.Text = "Server IP:";
            // 
            // button3
            // 
            this.button3.Location = new System.Drawing.Point(328, 248);
            this.button3.Name = "button3";
            this.button3.Size = new System.Drawing.Size(133, 44);
            this.button3.TabIndex = 0;
            this.button3.Text = "Save";
            this.button3.UseVisualStyleBackColor = true;
            this.button3.Click += new System.EventHandler(this.button3_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(814, 341);
            this.Controls.Add(this.tabControl1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.Name = "Form1";
            this.Text = "GalaxyRP Launcher";
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.tabControl1.ResumeLayout(false);
            this.tabPage1.ResumeLayout(false);
            this.tabPage2.ResumeLayout(false);
            this.tabPage2.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ListBox listBox1;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.ProgressBar progressBar1;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.Label label_last_changed;
        private System.Windows.Forms.Label label_version_number;
        private System.Windows.Forms.Label label_filename;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TabControl tabControl1;
        private System.Windows.Forms.TabPage tabPage1;
        private System.Windows.Forms.TabPage tabPage2;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Button button3;
        private System.Windows.Forms.Label label_filesize;
        private System.Windows.Forms.ComboBox comboBox_client_mod;
        private System.Windows.Forms.TextBox textBox_google_drive_link;
        private System.Windows.Forms.TextBox textBox_server_ip;
        private System.Windows.Forms.TextBox textBox_resolution_y;
        private System.Windows.Forms.TextBox textBox_resolution_x;
        private System.Windows.Forms.Label label10;
        private System.Windows.Forms.Label label9;
        private System.Windows.Forms.Button button4;
        private System.Windows.Forms.ComboBox comboBox_server_selection;
        private System.Windows.Forms.TextBox textBox_server_name_2;
        private System.Windows.Forms.Label label12;
        private System.Windows.Forms.TextBox textBox_server_ip_2;
        private System.Windows.Forms.Label label13;
        private System.Windows.Forms.TextBox textBox_server_name;
        private System.Windows.Forms.Label label11;
        private System.Windows.Forms.Label label14;
        private System.Windows.Forms.TextBox textBox_other_arguments;
        private System.Windows.Forms.Button button5;
    }
}

