using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;

namespace DobsonLibrary.DataAccess
{
    class KeywordDA : BaseDA
    {

        //Checks the table to seee if the keyword exists and if it does then it returns the keyword Id.
        //If the keyword does not exist then it adds it to the table and returns the keyword Id.
        private int addKeywordAndGetId(string newKeyword)
        {
            long keywordCount = (long)performScalarQuery(SQLQueries.getKeywordCountQuery,
                                                       new string[]{"keywordString"},
                                                       new object[]{newKeyword});

            if (keywordCount == 0)
            {
                performNonQuery(SQLQueries.insertKeywordQuery,
                                new string[] { "keywordString" },
                                new object[] { newKeyword });
            }

            return (int)performScalarQuery(SQLQueries.getKeywordIdQuery,
                                           new string[] { "keywordString" },
                                           new object[] { newKeyword });
        }

        //If the searchId is not a negative number (which it shouldn't be because it is serialized in the database)
        //then for each one of the keywords in that search add the search id and keyword id to the search keyword database.
        //The searchId is going to be the same for each keywordId
        public void addSearchKeywords(Search search)
        {
            if (search.searchID == -1) throw new ArgumentException("SearchID must be non-negative");

            foreach (string keyword in search.keywords)
            {
                int keywordId = addKeywordAndGetId(keyword);
                performNonQuery(SQLQueries.insertSearchKeywordQuery,
                                new string[] { "searchId", "keywordId" },
                                new object[] { search.searchID, keywordId });
            }
        }
    }
}
