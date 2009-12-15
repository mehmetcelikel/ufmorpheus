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
        //creates the actual connection to the database and then opens that connection
        public static NpgsqlConnection createOpenConnection(DatabaseConnection thisConnection)
        {
            NpgsqlConnection conn = new NpgsqlConnection(thisConnection.toString());
            conn.Open();
            return conn;
        }

    }
}
