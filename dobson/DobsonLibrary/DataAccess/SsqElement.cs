using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;
using DobsonLibrary.Domain;

namespace DobsonLibrary.DataAccess
{
    public class SsqElement
    {
        public int ClassId{get;set;}
        public int IndividualId { get; set; }
        public Context ElementContext{get; set;}
        public string Individual { get; set; }
        public List<Modifier> Modifiers { get; set; }
        
        public SsqElement(string individual, int classid, int individualid, Context context)
        {
            this.ClassId = classid;
            this.IndividualId = individualid;
            this.ElementContext = context;
            this.Individual = individual;
            this.Modifiers = new List<Modifier>();
        }

        public bool Compare(SsqElement e)
        {
            if (e.ClassId != this.ClassId)
                return false;

            if (e.ElementContext.contextID != this.ElementContext.contextID)
                return false;

            if (e.Individual != this.Individual)
                return false;

            if (e.IndividualId != this.IndividualId)
                return false;

            return true;
        }

    }
}
