namespace DobsonGUI
{
    partial class Main
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
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Main));
            this.statusStrip1 = new System.Windows.Forms.StatusStrip();
            this.toolStripStatusLabel1 = new System.Windows.Forms.ToolStripStatusLabel();
            this.toolStripProgressBar1 = new System.Windows.Forms.ToolStripProgressBar();
            this.menuStrip1 = new System.Windows.Forms.MenuStrip();
            this.optionsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.startButton = new System.Windows.Forms.Button();
            this.answerTextBox = new System.Windows.Forms.TextBox();
            this.answerLabel = new System.Windows.Forms.Label();
            this.queryTextBox = new System.Windows.Forms.TextBox();
            this.queryLabel = new System.Windows.Forms.Label();
            this.realmTextBox = new System.Windows.Forms.TextBox();
            this.realmLabel = new System.Windows.Forms.Label();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.removeButton = new System.Windows.Forms.Button();
            this.label3 = new System.Windows.Forms.Label();
            this.outputListBox = new System.Windows.Forms.ListBox();
            this.label5 = new System.Windows.Forms.Label();
            this.inputListBox = new System.Windows.Forms.ListBox();
            this.selectionMenu = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.inItem = new System.Windows.Forms.ToolStripMenuItem();
            this.contextMenuDropDown = new System.Windows.Forms.ToolStripComboBox();
            this.classMenuDropDown = new System.Windows.Forms.ToolStripComboBox();
            this.inputDoneToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.inCancelToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.outItem = new System.Windows.Forms.ToolStripMenuItem();
            this.outContextMenuDropDown = new System.Windows.Forms.ToolStripComboBox();
            this.outClassMenuDropDown = new System.Windows.Forms.ToolStripComboBox();
            this.outputDoneToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.outCancelToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.modifierItem = new System.Windows.Forms.ToolStripMenuItem();
            this.subjectMenuDropDown = new System.Windows.Forms.ToolStripComboBox();
            this.modifierDoneToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.modifierCancelToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.cancelButton = new System.Windows.Forms.Button();
            this.statusStrip1.SuspendLayout();
            this.menuStrip1.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.selectionMenu.SuspendLayout();
            this.SuspendLayout();
            // 
            // statusStrip1
            // 
            this.statusStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripStatusLabel1,
            this.toolStripProgressBar1});
            this.statusStrip1.Location = new System.Drawing.Point(0, 350);
            this.statusStrip1.Name = "statusStrip1";
            this.statusStrip1.Size = new System.Drawing.Size(646, 22);
            this.statusStrip1.TabIndex = 2;
            this.statusStrip1.Text = "statusStrip1";
            // 
            // toolStripStatusLabel1
            // 
            this.toolStripStatusLabel1.Name = "toolStripStatusLabel1";
            this.toolStripStatusLabel1.Size = new System.Drawing.Size(0, 17);
            // 
            // toolStripProgressBar1
            // 
            this.toolStripProgressBar1.Name = "toolStripProgressBar1";
            this.toolStripProgressBar1.Size = new System.Drawing.Size(100, 16);
            this.toolStripProgressBar1.Style = System.Windows.Forms.ProgressBarStyle.Marquee;
            this.toolStripProgressBar1.Visible = false;
            // 
            // menuStrip1
            // 
            this.menuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.optionsToolStripMenuItem});
            this.menuStrip1.Location = new System.Drawing.Point(0, 0);
            this.menuStrip1.Name = "menuStrip1";
            this.menuStrip1.Size = new System.Drawing.Size(646, 24);
            this.menuStrip1.TabIndex = 3;
            this.menuStrip1.Text = "menuStrip1";
            // 
            // optionsToolStripMenuItem
            // 
            this.optionsToolStripMenuItem.Name = "optionsToolStripMenuItem";
            this.optionsToolStripMenuItem.Size = new System.Drawing.Size(81, 20);
            this.optionsToolStripMenuItem.Text = "Connection";
            this.optionsToolStripMenuItem.Click += new System.EventHandler(this.optionsToolStripMenuItem_Click);
            // 
            // startButton
            // 
            this.startButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.startButton.Enabled = false;
            this.startButton.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.startButton.Location = new System.Drawing.Point(208, 307);
            this.startButton.Name = "startButton";
            this.startButton.Size = new System.Drawing.Size(115, 40);
            this.startButton.TabIndex = 12;
            this.startButton.Text = "START";
            this.startButton.UseVisualStyleBackColor = true;
            this.startButton.Click += new System.EventHandler(this.startButton_Click);
            // 
            // answerTextBox
            // 
            this.answerTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.answerTextBox.Location = new System.Drawing.Point(57, 275);
            this.answerTextBox.Name = "answerTextBox";
            this.answerTextBox.Size = new System.Drawing.Size(547, 20);
            this.answerTextBox.TabIndex = 11;
            // 
            // answerLabel
            // 
            this.answerLabel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.answerLabel.AutoSize = true;
            this.answerLabel.Location = new System.Drawing.Point(7, 278);
            this.answerLabel.Name = "answerLabel";
            this.answerLabel.Size = new System.Drawing.Size(42, 13);
            this.answerLabel.TabIndex = 21;
            this.answerLabel.Text = "Answer";
            // 
            // queryTextBox
            // 
            this.queryTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.queryTextBox.Location = new System.Drawing.Point(57, 61);
            this.queryTextBox.Name = "queryTextBox";
            this.queryTextBox.Size = new System.Drawing.Size(547, 20);
            this.queryTextBox.TabIndex = 2;
            this.queryTextBox.Click += new System.EventHandler(this.queryTextBox_Click);
            // 
            // queryLabel
            // 
            this.queryLabel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.queryLabel.AutoSize = true;
            this.queryLabel.Location = new System.Drawing.Point(14, 64);
            this.queryLabel.Name = "queryLabel";
            this.queryLabel.Size = new System.Drawing.Size(35, 13);
            this.queryLabel.TabIndex = 14;
            this.queryLabel.Text = "Query";
            // 
            // realmTextBox
            // 
            this.realmTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.realmTextBox.AutoCompleteMode = System.Windows.Forms.AutoCompleteMode.Suggest;
            this.realmTextBox.AutoCompleteSource = System.Windows.Forms.AutoCompleteSource.CustomSource;
            this.realmTextBox.Location = new System.Drawing.Point(57, 34);
            this.realmTextBox.Name = "realmTextBox";
            this.realmTextBox.Size = new System.Drawing.Size(547, 20);
            this.realmTextBox.TabIndex = 1;
            // 
            // realmLabel
            // 
            this.realmLabel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.realmLabel.AutoSize = true;
            this.realmLabel.Location = new System.Drawing.Point(12, 37);
            this.realmLabel.Name = "realmLabel";
            this.realmLabel.Size = new System.Drawing.Size(37, 13);
            this.realmLabel.TabIndex = 12;
            this.realmLabel.Text = "Realm";
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.removeButton);
            this.groupBox1.Controls.Add(this.label3);
            this.groupBox1.Controls.Add(this.outputListBox);
            this.groupBox1.Controls.Add(this.label5);
            this.groupBox1.Controls.Add(this.inputListBox);
            this.groupBox1.Location = new System.Drawing.Point(57, 88);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(547, 182);
            this.groupBox1.TabIndex = 31;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "SSQ";
            // 
            // removeButton
            // 
            this.removeButton.Location = new System.Drawing.Point(463, 150);
            this.removeButton.Name = "removeButton";
            this.removeButton.Size = new System.Drawing.Size(78, 23);
            this.removeButton.TabIndex = 10;
            this.removeButton.Text = "Remove";
            this.removeButton.UseVisualStyleBackColor = true;
            this.removeButton.Click += new System.EventHandler(this.removeButton_Click);
            // 
            // label3
            // 
            this.label3.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(281, 20);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(44, 13);
            this.label3.TabIndex = 43;
            this.label3.Text = "Outputs";
            // 
            // outputListBox
            // 
            this.outputListBox.FormattingEnabled = true;
            this.outputListBox.Location = new System.Drawing.Point(281, 36);
            this.outputListBox.Name = "outputListBox";
            this.outputListBox.Size = new System.Drawing.Size(260, 108);
            this.outputListBox.TabIndex = 9;
            this.outputListBox.SelectedIndexChanged += new System.EventHandler(this.outputListBox_SelectedIndexChanged);
            // 
            // label5
            // 
            this.label5.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(6, 20);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(36, 13);
            this.label5.TabIndex = 38;
            this.label5.Text = "Inputs";
            // 
            // inputListBox
            // 
            this.inputListBox.FormattingEnabled = true;
            this.inputListBox.Location = new System.Drawing.Point(6, 36);
            this.inputListBox.Name = "inputListBox";
            this.inputListBox.Size = new System.Drawing.Size(269, 108);
            this.inputListBox.TabIndex = 8;
            this.inputListBox.SelectedIndexChanged += new System.EventHandler(this.inputListBox_SelectedIndexChanged);
            // 
            // selectionMenu
            // 
            this.selectionMenu.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.inItem,
            this.outItem,
            this.modifierItem});
            this.selectionMenu.Name = "selectionMenu";
            this.selectionMenu.Size = new System.Drawing.Size(153, 92);
            // 
            // inItem
            // 
            this.inItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.contextMenuDropDown,
            this.classMenuDropDown,
            this.inputDoneToolStripMenuItem,
            this.inCancelToolStripMenuItem});
            this.inItem.Name = "inItem";
            this.inItem.Size = new System.Drawing.Size(152, 22);
            this.inItem.Text = "In";
            // 
            // contextMenuDropDown
            // 
            this.contextMenuDropDown.AutoCompleteMode = System.Windows.Forms.AutoCompleteMode.SuggestAppend;
            this.contextMenuDropDown.AutoCompleteSource = System.Windows.Forms.AutoCompleteSource.ListItems;
            this.contextMenuDropDown.Name = "contextMenuDropDown";
            this.contextMenuDropDown.Size = new System.Drawing.Size(121, 23);
            this.contextMenuDropDown.Text = "Context";
            // 
            // classMenuDropDown
            // 
            this.classMenuDropDown.AutoCompleteMode = System.Windows.Forms.AutoCompleteMode.SuggestAppend;
            this.classMenuDropDown.AutoCompleteSource = System.Windows.Forms.AutoCompleteSource.ListItems;
            this.classMenuDropDown.Name = "classMenuDropDown";
            this.classMenuDropDown.Size = new System.Drawing.Size(121, 23);
            this.classMenuDropDown.Text = "Class";
            // 
            // inputDoneToolStripMenuItem
            // 
            this.inputDoneToolStripMenuItem.Name = "inputDoneToolStripMenuItem";
            this.inputDoneToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.inputDoneToolStripMenuItem.Text = "Done";
            this.inputDoneToolStripMenuItem.Click += new System.EventHandler(this.inputDoneToolStripMenuItem_Click);
            // 
            // inCancelToolStripMenuItem
            // 
            this.inCancelToolStripMenuItem.Name = "inCancelToolStripMenuItem";
            this.inCancelToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.inCancelToolStripMenuItem.Text = "Cancel";
            this.inCancelToolStripMenuItem.Click += new System.EventHandler(this.outCancelToolStripMenuItem_Click);
            // 
            // outItem
            // 
            this.outItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.outContextMenuDropDown,
            this.outClassMenuDropDown,
            this.outputDoneToolStripMenuItem,
            this.outCancelToolStripMenuItem});
            this.outItem.Name = "outItem";
            this.outItem.Size = new System.Drawing.Size(152, 22);
            this.outItem.Text = "Out";
            // 
            // outContextMenuDropDown
            // 
            this.outContextMenuDropDown.AutoCompleteMode = System.Windows.Forms.AutoCompleteMode.SuggestAppend;
            this.outContextMenuDropDown.AutoCompleteSource = System.Windows.Forms.AutoCompleteSource.ListItems;
            this.outContextMenuDropDown.Name = "outContextMenuDropDown";
            this.outContextMenuDropDown.Size = new System.Drawing.Size(121, 23);
            this.outContextMenuDropDown.Text = "Context";
            // 
            // outClassMenuDropDown
            // 
            this.outClassMenuDropDown.AutoCompleteMode = System.Windows.Forms.AutoCompleteMode.SuggestAppend;
            this.outClassMenuDropDown.AutoCompleteSource = System.Windows.Forms.AutoCompleteSource.ListItems;
            this.outClassMenuDropDown.Name = "outClassMenuDropDown";
            this.outClassMenuDropDown.Size = new System.Drawing.Size(121, 23);
            this.outClassMenuDropDown.Text = "Class";
            // 
            // outputDoneToolStripMenuItem
            // 
            this.outputDoneToolStripMenuItem.Name = "outputDoneToolStripMenuItem";
            this.outputDoneToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.outputDoneToolStripMenuItem.Text = "Done";
            this.outputDoneToolStripMenuItem.Click += new System.EventHandler(this.outputDoneToolStripMenuItem_Click);
            // 
            // outCancelToolStripMenuItem
            // 
            this.outCancelToolStripMenuItem.Name = "outCancelToolStripMenuItem";
            this.outCancelToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.outCancelToolStripMenuItem.Text = "Cancel";
            this.outCancelToolStripMenuItem.Click += new System.EventHandler(this.outCancelToolStripMenuItem_Click_1);
            // 
            // modifierItem
            // 
            this.modifierItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.subjectMenuDropDown,
            this.modifierDoneToolStripMenuItem,
            this.modifierCancelToolStripMenuItem});
            this.modifierItem.Name = "modifierItem";
            this.modifierItem.Size = new System.Drawing.Size(152, 22);
            this.modifierItem.Text = "Modifer";
            // 
            // subjectMenuDropDown
            // 
            this.subjectMenuDropDown.AutoCompleteMode = System.Windows.Forms.AutoCompleteMode.SuggestAppend;
            this.subjectMenuDropDown.AutoCompleteSource = System.Windows.Forms.AutoCompleteSource.ListItems;
            this.subjectMenuDropDown.Name = "subjectMenuDropDown";
            this.subjectMenuDropDown.Size = new System.Drawing.Size(121, 23);
            this.subjectMenuDropDown.Text = "Subject";
            // 
            // modifierDoneToolStripMenuItem
            // 
            this.modifierDoneToolStripMenuItem.Name = "modifierDoneToolStripMenuItem";
            this.modifierDoneToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.modifierDoneToolStripMenuItem.Text = "Done";
            this.modifierDoneToolStripMenuItem.Click += new System.EventHandler(this.modifierDoneToolStripMenuItem_Click);
            // 
            // modifierCancelToolStripMenuItem
            // 
            this.modifierCancelToolStripMenuItem.Name = "modifierCancelToolStripMenuItem";
            this.modifierCancelToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.modifierCancelToolStripMenuItem.Text = "Cancel";
            this.modifierCancelToolStripMenuItem.Click += new System.EventHandler(this.modifierCancelToolStripMenuItem_Click);
            // 
            // cancelButton
            // 
            this.cancelButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.cancelButton.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.cancelButton.Location = new System.Drawing.Point(329, 307);
            this.cancelButton.Name = "cancelButton";
            this.cancelButton.Size = new System.Drawing.Size(115, 40);
            this.cancelButton.TabIndex = 32;
            this.cancelButton.Text = "CANCEL";
            this.cancelButton.UseVisualStyleBackColor = true;
            this.cancelButton.Click += new System.EventHandler(this.cancelButton_Click);
            // 
            // Main
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(646, 372);
            this.Controls.Add(this.cancelButton);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.realmTextBox);
            this.Controls.Add(this.answerTextBox);
            this.Controls.Add(this.statusStrip1);
            this.Controls.Add(this.startButton);
            this.Controls.Add(this.answerLabel);
            this.Controls.Add(this.queryTextBox);
            this.Controls.Add(this.menuStrip1);
            this.Controls.Add(this.queryLabel);
            this.Controls.Add(this.realmLabel);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MainMenuStrip = this.menuStrip1;
            this.Name = "Main";
            this.Text = "Dobson";
            this.Load += new System.EventHandler(this.Main_Load);
            this.statusStrip1.ResumeLayout(false);
            this.statusStrip1.PerformLayout();
            this.menuStrip1.ResumeLayout(false);
            this.menuStrip1.PerformLayout();
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.selectionMenu.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.StatusStrip statusStrip1;
        private System.Windows.Forms.ToolStripStatusLabel toolStripStatusLabel1;
        private System.Windows.Forms.ToolStripProgressBar toolStripProgressBar1;
        private System.Windows.Forms.MenuStrip menuStrip1;
        private System.Windows.Forms.ToolStripMenuItem optionsToolStripMenuItem;
        private System.Windows.Forms.Button startButton;
        private System.Windows.Forms.TextBox answerTextBox;
        private System.Windows.Forms.Label answerLabel;
        private System.Windows.Forms.TextBox queryTextBox;
        private System.Windows.Forms.Label queryLabel;
        private System.Windows.Forms.TextBox realmTextBox;
        private System.Windows.Forms.Label realmLabel;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.ListBox inputListBox;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.ListBox outputListBox;
        private System.Windows.Forms.Button removeButton;
        private System.Windows.Forms.ContextMenuStrip selectionMenu;
        private System.Windows.Forms.ToolStripMenuItem inItem;
        private System.Windows.Forms.ToolStripMenuItem outItem;
        private System.Windows.Forms.ToolStripMenuItem modifierItem;
        private System.Windows.Forms.ToolStripComboBox contextMenuDropDown;
        private System.Windows.Forms.ToolStripComboBox outContextMenuDropDown;
        private System.Windows.Forms.ToolStripComboBox classMenuDropDown;
        private System.Windows.Forms.ToolStripComboBox subjectMenuDropDown;
        private System.Windows.Forms.ToolStripMenuItem inputDoneToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem outputDoneToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem modifierDoneToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem inCancelToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem outCancelToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem modifierCancelToolStripMenuItem;
        private System.Windows.Forms.Button cancelButton;
        private System.Windows.Forms.ToolStripComboBox outClassMenuDropDown;
    }
}

