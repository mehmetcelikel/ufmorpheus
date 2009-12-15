using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class SearchDA : BaseDA
    {

        // Add a search to the database
        public void addSearch(Search search)
        {
            // Insert information into Search table
            performNonQuery(SQLQueries.insertSearchQuery,
                            new string[] { "searchEngine", "searchString" },
                            new object[] { search.SearchEngine, search.searchString });

            // Get the new searches searchId
            search.searchID = (long)performScalarQuery(SQLQueries.getLastSearchIdQuery);

            using (KeywordDA keywordDA = new KeywordDA())
            {
                keywordDA.addSearchKeywords(search);
            }

            using (PathEntryDA pathEntryDA = new PathEntryDA())
            {
                pathEntryDA.addSearchPath(search);
            }

        }

        //gets all of the searches added to the database and parses them up into a list of searches.
        public IList<Search> getAllSearches()
        {
            NpgsqlDataReader allResults = performQuery(SQLQueries.getAllSearchesQuery);

            IList<Search> returnList = new List<Search>();
            Search currentSearch = null;

            while (allResults.Read())
            {
                int searchId = allResults.GetInt32(allResults.GetOrdinal("searchId"));

                if (currentSearch != null && searchId != currentSearch.searchID)
                {
                    returnList.Add(currentSearch);
                    currentSearch = null;
                }

                if (currentSearch == null)
                {
                    currentSearch = new Search();
                    currentSearch.SearchEngine = allResults.GetString(allResults.GetOrdinal("searchEngine"));
                    currentSearch.searchString = allResults.GetString(allResults.GetOrdinal("searchString"));
                    currentSearch.searchID = searchId;
                }

                PathEntry entry = new PathEntry();
                entry.website = allResults.GetString(allResults.GetOrdinal("url"));
                entry.order = allResults.GetInt32(allResults.GetOrdinal("entryOrder"));
                currentSearch.path.Add(entry);
            }

            if (currentSearch != null)
            {
                returnList.Add(currentSearch);
            }

            return returnList;

        }

    }
}
