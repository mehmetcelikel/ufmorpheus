using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Website
    {
        
        //public string url { get; set; }
        public string baseUrl { get; set; }

        public string queryString { get; set; }

        public string highlighted { get; set; }
        
        public string comments { get; set; }

        public Word word { get; set; }

        public Website()
        {
            baseUrl = "";
            queryString = "";
            highlighted = "";
            comments = "";
            word = new Word();
        }

    }
}
