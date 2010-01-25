using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;

namespace DobsonLibrary.DataAccess
{
    public class SsqElementManager
    {
        private Hashtable ssqInputs = new Hashtable();
        private Hashtable highlights = new Hashtable();

        public  void AddSsqInput(SsqElement elem)
        {
            if (!ssqInputs.ContainsKey(elem.ClassId))
                ssqInputs[elem.ClassId] = new List<SsqElement>();

            List<SsqElement> elems = (List<SsqElement>)ssqInputs[elem.ClassId];

            elems.Add(elem);

            ssqInputs[elem.ClassId] = elems;
        }
        public void AddHighlight(SsqElement elem)
        {
            if (!highlights.ContainsKey(elem.ClassId))
                highlights[elem.ClassId] = new List<SsqElement>();

            List<SsqElement> elems = (List<SsqElement>)highlights[elem.ClassId];

            elems.Add(elem);

            highlights[elem.ClassId] = elems;
        }

        public SsqElement FindSsqInput(int classID, string name)
        {
            List<SsqElement> elems = (List<SsqElement>)ssqInputs[classID];

            SsqElement elem = getElement(name, elems);

            return elem;
        }
        public SsqElement FindHighlight(int classID, string name)
        {
            List<SsqElement> elems = (List<SsqElement>)highlights[classID];

            SsqElement elem = getElement(name, elems);

            return elem;
        }
        private SsqElement getElement(string name, List<SsqElement> elems)
        {
            if (elems == null)
                return null;

            //TODO: Will this really work?
            name = name.ToUpper();

            int minDistance = Int32.MaxValue;

            SsqElement minElement = null;

            foreach (SsqElement e in elems)
            {
                int r = getEditDistance(e.Individual.ToUpper(), name);

                if (r < minDistance)
                {
                    minDistance = r;
                    minElement = e;
                }
            }

            return minElement;
        }

        public void Clear()
        {
            this.highlights.Clear();
            this.ssqInputs.Clear();
        }

        //use's string edit distance algorithm (wikipedia)
        private int getEditDistance(string s1, string s2)
        {
            if (s1 == "" || s2 == "")
                return int.MaxValue;

            int [,] table = new int[s1.Length,s2.Length];

            for (int i1 = 1; i1 < s2.Length; i1++)
            {
                for (int i2 = 1; i2 < s1.Length; i2++)
                {
                    if (s1[i2] == s2[i1])
                    {
                        table[i2,i1] = table[i2 - 1,i1 - 1];
                    }
                    else
                    {
                        //the first case is for the number of deletes, the second is for inserts and the third 
                        //is for substitutions
                        table[i2,i1] = Math.Min(table[i2 - 1,i1] + 1, Math.Min(table[i2,i1 - 1] + 1, table[i2 - 1,i1 - 1] + 1));
                    }
                }
            }

            return table[s1.Length-2,s2.Length-1];
        }
    }
}
