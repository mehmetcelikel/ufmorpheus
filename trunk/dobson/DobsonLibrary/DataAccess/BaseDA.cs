using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Npgsql;
using DobsonLibrary.Domain;
using System.IO;

namespace DobsonLibrary.DataAccess
{
    class BaseDA : IDisposable
    {

        private static String debugPath = "";

        private static DatabaseConnection databaseConnection = new DatabaseConnection(DatabaseConnection.DBName.Morpheus3DB);
        private static DatabaseConnection sdbConnection = new DatabaseConnection(DatabaseConnection.DBName.sdb);
        private static bool hasNewConnection = true;

        //sets the database connection to the parameter
        protected DatabaseConnection DatabaseConnection

        {
            set
            {
                databaseConnection = value;
                hasNewConnection = true;
            }
            get
            {
                return databaseConnection;
            }

        }

        //The path where the debug information is saved
        public static String DebugPath
        {
            set
            {
                debugPath = value;
            }
        }


        private NpgsqlConnection conn = null;

        //gets the newest connection needed
        private NpgsqlConnection getConnection(DatabaseConnection.DBName name)
        {

            //if a new connection is needed and one is already made then the old one needs to be closed
            if (hasNewConnection && conn != null)
            {
                conn.Close();
                conn = null;
            }
            hasNewConnection = false;
            //If there is no connection then this makes the connection and opens it
            if (conn == null)
            {
                if (name == DatabaseConnection.DBName.Morpheus3DB)
                    conn = NpgsqlConnectionFactory.createOpenConnection(databaseConnection);
                else
                    conn = NpgsqlConnectionFactory.createOpenConnection(sdbConnection);
            }
            return conn;
        }
        //makes a postgres command out of the information given
        private NpgsqlCommand makeCommand(string query)
        {
            //printQuery(query);
            return new NpgsqlCommand(query, getConnection(DatabaseConnection.DBName.Morpheus3DB));
        }
        private NpgsqlCommand makeCommand(string query, DatabaseConnection.DBName name)
        {
            //printQuery(query);
            return new NpgsqlCommand(query, getConnection(name));
        }

        //performs a query where no result is needed.  ex. insert into table.
        protected int performNonQuery(string query)
        {
            //printQuery(query);

            return makeCommand(query).ExecuteNonQuery();
        }

        //performs a query where no result is needed.  ex. insert into table.
        protected int performNonQuery(string query, string[] paramNames, object[] paramValues)
        {
            if (paramNames.Length != paramValues.Length) throw new ArgumentException("paramNames and paramValues must have same length");
            NpgsqlCommand command = makeCommand(query);

            for (int i = 0; i < paramNames.Length; i++)
            {
                command.Parameters.Add(paramNames[i], paramValues[i]);
            }
            //printQuery(command.CommandText);

            return command.ExecuteNonQuery();
        }
        //performs a query where a result is needed.   ex. select * from table
        protected NpgsqlDataReader performQuery(string query)
        {
            //printQuery(query);

            return makeCommand(query).ExecuteReader();
        }
        //performs a query where a result is needed.   ex. select * from table
        protected NpgsqlDataReader performQuery(string query, DatabaseConnection.DBName name)
        {
            //printQuery(query);

            return makeCommand(query,name).ExecuteReader();
        }

        //performs a query where a result is needed.   ex. select * from table
        protected NpgsqlDataReader performQuery(string query, string[] paramNames, object[] paramValues)
        {
            if (paramNames.Length != paramValues.Length) throw new ArgumentException("paramNames and paramValues must have same length");
            NpgsqlCommand command = makeCommand(query);

            for (int i = 0; i < paramNames.Length; i++)
            {
                command.Parameters.Add(paramNames[i], paramValues[i]);
            }
            //printQuery(command.CommandText);
            return command.ExecuteReader();
        }

        //returns one cell of data.  ex. select count (*)
        protected object performScalarQuery(string query)
        {
            //printQuery(query);

            return makeCommand(query).ExecuteScalar();
        }

        //returns one cell of data.  ex. select count (*)
        protected object performScalarQuery(string query, string[] paramNames, object[] paramValues)
        {
            if (paramNames.Length != paramValues.Length) throw new ArgumentException("paramNames and paramValues must have same length");
            NpgsqlCommand command = makeCommand(query);

            for (int i = 0; i < paramNames.Length; i++)
            {
                command.Parameters.Add(paramNames[i], paramValues[i]);
            }

            //printQuery(command.CommandText);
            return command.ExecuteScalar();
        }

        ////closes down the connection
        public virtual void Dispose()
        {
            if (conn != null)
                conn.Close();
        }

        /*prints the query for debuging purposes into the databaseDebug.txt, appending the new query every time
        protected static void printQuery(string query)
        {
            StreamWriter s = new StreamWriter(debugPath + "\\databaseDebug.txt", true);

            s.WriteLine(query);
            s.WriteLine();
            s.Close();
        }
         * */
        protected bool testConnection()
        {
            try
            {
                NpgsqlConnection thisConnection = getConnection(DatabaseConnection.DBName.Morpheus3DB);
                return true;
            }
            catch ( Exception)
            {
                return false;
            }
        }
    }
}
