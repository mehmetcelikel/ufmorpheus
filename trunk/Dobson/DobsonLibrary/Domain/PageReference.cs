
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class PageReference
    {
        public string queryString { get; set; }
        public int pageRefID { get; set; }
        public int pageID { get; set; }
        public int queryID { get; set; }
        public string pagesource { get; set; }
        public string destinationUrl { get; set; }
        public long timestamp { get; set; }
        public string formxpath { get; set; }

        public PageReference()
        {
            queryString = "";
            pageRefID = -1;
            pageID = -1;
            queryID = -1;
            pagesource = "";
            destinationUrl = "";
            formxpath = "";
            timestamp = -1L;

        }
        public PageReference(string query, int pageid, int queryid,string pagesrc,long timestamp,string xpath, string destinationUrl)
        {
            formxpath = xpath;
            queryString = query;
            pageRefID = -1;
            pageID = pageid;
            queryID = queryid;
            this.pagesource = pagesrc;
            this.destinationUrl = destinationUrl;
            this.timestamp = timestamp;
        }
    }
}
