using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using DobsonLibrary.DataAccess;

namespace DobsonLibrary.Business
{
    public class ModifierBL
    {
        public void insertModifier(Modifier mod)
        {
            using (ModifierDA da = new ModifierDA())
            {
                da.insertModifier(mod);
            }
        }
    }
}
