using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using DobsonLibrary.DataAccess;

namespace DobsonLibrary.Business
{
    public class InputBL
    {
        public void insertInput(Input currentInput)
        {
            using (InputDA da = new InputDA())
            {
                da.insertInput(currentInput);
            }
        }
    }
}
