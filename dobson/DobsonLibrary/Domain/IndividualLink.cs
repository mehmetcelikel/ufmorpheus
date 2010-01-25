using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class IndividualLink
    {
        public Query query { get; set; }
        public Answer answer { get; set; }
        public string inOrOut { get; set; }
        public long individualID { get; set; }

        public IndividualLink()
        {
            query = null;
            answer = null;
            inOrOut = "";
            individualID = -1;
        }
        public IndividualLink(long thisIndividualID, Query thisQuery, string inputOrOutput)
        {
            query = thisQuery;
            answer = null;
            inOrOut = inputOrOutput;
            individualID = thisIndividualID;
        }
        public IndividualLink(long thisIndividualID, Answer thisAnswer, string inputOrOutput)
        {
            query = null;
            answer = thisAnswer;
            inOrOut = inputOrOutput;
            individualID = thisIndividualID;
        }
    }
}
