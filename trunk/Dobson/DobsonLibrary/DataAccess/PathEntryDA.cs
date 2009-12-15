using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;

namespace DobsonLibrary.DataAccess
{
    class PathEntryDA : BaseDA
    {

        //Checks the table to seee if the website url exists and if it does then it returns the websiteId.
        //If the url does not exist then it adds it to the table and returns the websiteId.
        private int addWebsiteAndGetId(string newWebsite)
        {
            long websiteCount = (long)performScalarQuery(SQLQueries.getWebsiteCountQuery,
                                                       new string[] { "url" },
                                                       new object[] { newWebsite });

            if (websiteCount == 0)
            {
                performNonQuery(SQLQueries.insertWebsiteQuery,
                                new string[] { "url" },
                                new object[] { newWebsite });
            }

            return (int)performScalarQuery(SQLQueries.getWebsiteIdQuery,
                                           new string[] { "url" },
                                           new object[] { newWebsite });
        }


        public void addSearchPath(Search search)
        {
            if (search.searchID == -1) throw new ArgumentException("SearchID must be non-negative");

            foreach (PathEntry path in search.path)
            {
                int websiteId = addWebsiteAndGetId(path.website);
                performNonQuery(SQLQueries.insertPathEntryQuery,
                                new string[] { "searchId", "websiteId", "entryOrder" },
                                new object[] { search.searchID, websiteId, path.order });
            }
        }

    }
}
