using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class DatabaseConnection
    {
        public string server { get; set; }
        public string username { get; set; }
        public string password { get; set; }
        public string database { get; set; }

        public int port { get; set; }


        public DatabaseConnection()
        {
            //server = "postgres.cise.ufl.edu";
            //port = 5432;
            server = "babylon.cise.ufl.edu";
            port = 5432;
            username = "postgres";
            password = "gotct";
            database = "Morpheus3DB";

        }

        public string toString()
        {
            //Server=postgres.cise.ufl.edu;Port=5432;User Id=mlance;Password=password1!;Database=testforseniorproject"
            StringBuilder returnString = new StringBuilder();
            returnString.Append("Server=");
            returnString.Append(server);
            returnString.Append(";Port=");
            returnString.Append(port);
            returnString.Append(";User Id=");
            returnString.Append(username);
            returnString.Append(";Password=");
            returnString.Append(password);
            returnString.Append(";Database=");
            returnString.Append(database);

            return returnString.ToString();
        }

    }
}
