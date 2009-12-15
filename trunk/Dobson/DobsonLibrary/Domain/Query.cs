using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Query
    {
        public string queryString { get; set; }
        public int queryID { get; set; }
        public int realmID { get; set; }
        public int userID { get; set; }

        public Query()
        {
            queryString = "";
            queryID = -1;
            realmID = -1;
            userID = -1;
        }
        public Query(string query, int realm, int userID)
        {
            queryString = query;
            realmID = realm;
            queryID = -1;
            this.userID = userID;
        }
    }
}
