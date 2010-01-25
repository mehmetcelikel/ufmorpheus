using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class PhraseBL
    {
        public Phrase insertPhraseWithIndividual(Phrase currentPhrase)
        {
            using (PhraseDA da = new PhraseDA())
            {
                return da.insertPhraseWithIndividual(currentPhrase);
            }
        }
       
    }
}