using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Npgsql;
using DobsonLibrary.Domain;

namespace DobsonLibrary.DataAccess
{
    static class NpgsqlConnectionFactory
    {
        private static NpgsqlConnection conn;

        //creates the actual connection to the database and then opens that connection
        public static NpgsqlConnection createOpenConnection(DatabaseConnection thisConnection)
        {
            conn = new NpgsqlConnection(thisConnection.toString());
            conn.Open();
            return conn;
        }

        public static void Close()
        {
            conn.Close();
        }
        public static void Open()
        {
            conn.Open();
        }
    }
}
