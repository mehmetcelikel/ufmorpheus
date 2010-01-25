using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class IndividualLinkBL
    {
        public void insertIndividualLink(IndividualLink link)
        {
            using (IndividualLinkDA da = new IndividualLinkDA())
            {
                da.insertInstanceLink(link);
            }
        }
    }
}