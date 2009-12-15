using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class ContextDA : BaseDA
    {
        public int insertContext(Context c)
        {
            return performNonQuery(SQLQueries.insertContextQuery,
              new string[] { "contextname" },
              new object[] { c.contextName });

        }
        public List<Context> getAllContexts()
        {
            NpgsqlDataReader allResults = performQuery(SQLQueries.getContextQuery);

            List<Context> returnList = new List<Context>();

            while (allResults.Read())
            {
                Context currentContext = new Context();
                currentContext.contextID = allResults.GetInt32(allResults.GetOrdinal("contextid"));
                currentContext.contextName = allResults.GetString(allResults.GetOrdinal("contextname"));
                returnList.Add(currentContext);
            }
            return returnList;
        }


    }
}



