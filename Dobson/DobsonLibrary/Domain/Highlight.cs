using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Highlight
    {
        public int beginOffset { get; set; }
        public int endOffset { get; set; }
        public int queryID { get; set; }
        public int highlightID { get; set; }
        public int answerID { get; set; }
        public string startxpath { get; set; }
        public string endxpath { get; set; }
        public string meetpoint { get; set; }
        public long timestamp { get; set; }
        public string url { get; set; }
        public string selection { get; set; }
        public int contextClassID { get; set; }
        public string pagesource { get; set; }

        public Highlight()
        {
            beginOffset = -1;
            endOffset = -1;
            queryID = -1;
            highlightID = -1;
            answerID = -1;
            startxpath = "";
            meetpoint = "";
            endxpath = "";
            timestamp = -1L;
            url = "";
            selection = "";
            contextClassID = -1;
            pagesource = "";
        }
        
        public Highlight(int highlightID, int answerID, int pagerefid, int beginOffset, int endOffset,
            string startxpath,string endxpath,string meetpoint,long timestamp,string url,string selection,int contextClassID,string pagesource)
        {
            this.highlightID = highlightID;
            this.answerID = answerID;
            this.endOffset = endOffset;
            this.beginOffset = beginOffset;
            this.queryID = queryID;
            this.startxpath = startxpath;
            this.meetpoint = meetpoint;
            this.endxpath = endxpath;
            this.timestamp = timestamp;
            this.url = url;
            this.selection = selection;
            this.contextClassID = contextClassID;
            this.pagesource = pagesource;
        }
    }
}
