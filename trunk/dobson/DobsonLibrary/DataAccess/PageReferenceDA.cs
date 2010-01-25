using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class PageReferenceDA : BaseDA
    {
        public long insertPageReference(PageReference currentPageReference)
        {
            object id = performScalarQuery(SQLQueries.getLastPageReferenceIdQuery);

            long pageRefId = 0;

            if (id != DBNull.Value)
                pageRefId = Convert.ToInt64(id) + 1;
            
            string queryString = null;
            
            if (currentPageReference.queryString.Trim() != "") 
                queryString = currentPageReference.queryString;
            
            performNonQuery(SQLQueries.insertPageReferenceQuery,
                new string[] { "querystring", "pageid", "queryid", "pagerefid","pagesrc","timestamp","formxpath","destinationurl" },
                new object[] { queryString, currentPageReference.pageID, currentPageReference.queryID, 
                    Convert.ToInt32(pageRefId),currentPageReference.pagesource,currentPageReference.timestamp,
                    currentPageReference.formxpath,currentPageReference.destinationUrl});
            return pageRefId;
        }

        internal void updatePageReferenceQuerystring(int prefid, string qs)
        {
            performNonQuery(SQLQueries.updatePageReferenceQuerystring,
                new string[] { "pagerefid", "querystring" },
                new object[] { prefid, qs });
        }
    }
}
