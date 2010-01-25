using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class SearchBL
    {

        public void addSearch(Search search)
        {

            //Since i am using "using .." the using statment will call da.Dispose no matter what happens.
            //This means that the connection will always be properly closed
            using (SearchDA da = new SearchDA())
            {


                for (int i = 0; i < search.path.Count; i++)
                {
                    search.path[i].order = i;
                }

                da.addSearch(search);
            }
        }

        public IList<Search> getAllSearches()
        {
            using (SearchDA da = new SearchDA())
            {
                return  da.getAllSearches();
            }
        }

        public IList<Search> getFilteredSearches(string searchString)
        {
            using (SearchDA da = new SearchDA())
            {
                return da.getAllSearches();
            }
        }

    }
}
