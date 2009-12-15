
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Page
    {
        public string baseURL { get; set; }
        public int pageID { get; set; }

        public Page()
        {
            baseURL = "";
            pageID = -1;
        }
        public Page(string url)
        {
            baseURL = url;
            pageID = -1;
        }
    }
}
