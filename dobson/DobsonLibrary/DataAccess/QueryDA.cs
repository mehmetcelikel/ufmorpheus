using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class QueryDA : BaseDA
    {
        public long insertQuery(Query currentQuery)
        {
            performNonQuery(SQLQueries.insertQueryQuery,
                            new string[] { "querystring", "realmid","userID" },
                            new object[] { currentQuery.queryString, currentQuery.realmID,currentQuery.userID });

            return (long)performScalarQuery(SQLQueries.getLastQueryIdQuery);
        }

        public List<Query> getAllQueries()
        {
            NpgsqlDataReader allResults = performQuery(SQLQueries.getAllQueriesQuery);

            List<Query> returnList = new List<Query>();

            while (allResults.Read())
            {
                Query currentQuery = new Query();
                currentQuery.userID = allResults.GetInt32(allResults.GetOrdinal("userID"));
                currentQuery.queryID = allResults.GetInt32(allResults.GetOrdinal("queryid"));
                currentQuery.realmID = allResults.GetInt32(allResults.GetOrdinal("realmid"));
                currentQuery.queryString = allResults.GetString(allResults.GetOrdinal("querystring"));
                returnList.Add(currentQuery);
            }
            return returnList;
            
        }
    }
}
