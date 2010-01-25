using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Modifier
    {
        public int ModifierId { get; set; }
        public string ModifierString { get; set; }
        public int Rank { get; set; }
        public int IndividualId { get; set; }
        
        public Modifier(int modifierId, string modifierString, int rank, int individualId)
        {
            this.ModifierId = modifierId;
            this.Rank = rank;
            this.ModifierString = modifierString;
            this.IndividualId = individualId;
           }


    }
}
