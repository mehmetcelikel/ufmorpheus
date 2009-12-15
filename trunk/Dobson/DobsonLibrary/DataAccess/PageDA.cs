
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class PageDA : BaseDA
    {
        public long insertPage(Page currentPage)
        {
            object id = performScalarQuery(SQLQueries.getLastPageIdQuery);
            
            long thisPageID = 0L;

            if (id != DBNull.Value)
                thisPageID = Convert.ToInt64(id) + 1;

            performNonQuery(SQLQueries.insertPageQuery,
                new string[] { "baseurl", "pageid" },
                            new object[] { currentPage.baseURL, Convert.ToInt32(thisPageID) });

            return thisPageID;
        }
    }
}
