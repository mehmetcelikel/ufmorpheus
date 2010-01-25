using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class QueryBL
    {
        public long insertQuery(Query currentQuery)
        {
            using (QueryDA da = new QueryDA())
            {
                return da.insertQuery(currentQuery);
            }
        }

        public List<Query> getAllQueries()
        {
            using (QueryDA da = new QueryDA())
            {
                return da.getAllQueries();
            }
        }
    }
}