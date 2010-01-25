using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using DobsonLibrary.DataAccess;

namespace DobsonLibrary.Business
{
    public class HighlightBL
    {
        public long insertHighlight(Highlight currentHL)
        {
            using (HighlightDA da = new HighlightDA())
            {
                return da.insertHighlight(currentHL);
            }
        }
    }
}
