using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;

namespace DobsonLibrary.DataAccess
{
    class DatabaseConnectionDA : BaseDA 
    {

        public void setConnection(DatabaseConnection newConnection)
        {
            base.DatabaseConnection = newConnection;
        }

        public DatabaseConnection getCurrentConnection()
        {
            return base.DatabaseConnection;
        }

        public new bool testConnection()
        {
            return base.testConnection();
        }

    }
}
