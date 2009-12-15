
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Answer
    {
        public string answerString { get; set; }
        public int answerID { get; set; }
        public int queryID { get; set; }
        public int realmID { get; set; }
        
        public Answer()
        {
            answerString = "";
            answerID = -1;
            queryID = -1;
            realmID = -1;
        
        }
        public Answer(string answer, int query, int realm)
        {
            answerString = answer;
            queryID = query;
            realmID = realm;
            answerID = -1;
        }
    }
}
