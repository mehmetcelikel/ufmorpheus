using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class PathEntry
    {
        //the website that got hit
        public string website { get; set; }

        //the order in whcih the website was hit
        public int order { get; set; }


    }
}
