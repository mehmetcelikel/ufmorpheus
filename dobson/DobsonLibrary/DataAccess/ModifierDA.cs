using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;

namespace DobsonLibrary.DataAccess
{
    class ModifierDA : BaseDA
    {
        public void insertModifier(Modifier mod)
        {
            //perform db insertion of modifier object
            performNonQuery(SQLQueries.insertModifierQuery,
                new string[] { "modifierstring", "rank" },
                new object[] { mod.ModifierString, mod.Rank });

            //perform insert of modifer-individual link
            performNonQuery(SQLQueries.insertHasModifierQuery,
                new string[] { "individualid" },
                new object[] { mod.IndividualId });
        }

    }
}
