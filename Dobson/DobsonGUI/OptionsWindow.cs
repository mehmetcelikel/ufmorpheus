using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using DobsonLibrary.Business;
using DobsonLibrary.Domain;
using System.IO;

namespace DobsonGUI
{
    public partial class OptionsWindow : Form
    {
        public DatabaseConnection currentConnection
        {
            get { return (DatabaseConnection)databaseConnectionBindingSource.DataSource; }
            set { databaseConnectionBindingSource.DataSource = value; }
        }

        public OptionsWindow()
        {
            string textLocation = "C:\\Program Files\\Dobson\\connection.txt";
            InitializeComponent();
            if (File.Exists(textLocation))
            {
                TextReader tr = new StreamReader(textLocation);
            }
            else
            {
                currentConnection = new DatabaseConnection();
            }
        }

        private void testConnectionButton_Click(object sender, EventArgs e)
        {
            DatabaseConnectionBL connectionBL = new DatabaseConnectionBL();
            DatabaseConnection connectionBeforeTest = connectionBL.getCurrentConnection();
            connectionBL.setConnection(currentConnection);
            testConnectionButton.Text = "Testing";
            testConnectionButton.Enabled = false;

            if (connectionBL.testConnection())
            {
                MessageBox.Show("This is a valid connection.");
                saveAllButton.Enabled = true;
                testConnectionButton.Enabled = true;
                testConnectionButton.Text = "Test Connection";
            }
            else
            {
                MessageBox.Show("This is NOT a valid connection");
                saveAllButton.Enabled = false;
                testConnectionButton.Enabled = true;
                testConnectionButton.Text = "Test Connection";
            }

            connectionBL.setConnection(connectionBeforeTest);
        }

        private void OptionsWindow_Load(object sender, EventArgs e)
        {

        }

    }
}
