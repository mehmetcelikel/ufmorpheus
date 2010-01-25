using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class DatabaseConnectionBL
    {

        public void setConnection(DatabaseConnection newConnection)
        {

            using (DatabaseConnectionDA da = new DatabaseConnectionDA())
            {
                da.setConnection(newConnection);
            }
        }

        public DatabaseConnection getCurrentConnection()
        {
            using (DatabaseConnectionDA da = new DatabaseConnectionDA())
            {
                return da.getCurrentConnection();
            }
        }

        public bool testConnection()
        {
            using (DatabaseConnectionDA da = new DatabaseConnectionDA())
            {
                return da.testConnection();
            }
        }


        public void CloseConnection()
        {
            using (DatabaseConnectionDA da = new DatabaseConnectionDA())
            {
                da.CloseConnection();
            }
            
        }

        public void OpenConnection()
        {
            using (DatabaseConnectionDA da = new DatabaseConnectionDA())
            {
                da.OpenConnection();
            }
        }
    }
}
