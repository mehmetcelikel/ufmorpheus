using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Search
    {

        public List<PathEntry> path { get; set; }
        public string[] keywords
        {
            get
            {
                return searchString.Split(new char[] { ' ', ',' });
            }
        }

        public string searchString { get; set; }
        public string SearchEngine { get; set; }

        public Int64 searchID { get; set; }

        public Search()
        {
            path = new List<PathEntry>();
            searchID = -1;
        }


    }
}
