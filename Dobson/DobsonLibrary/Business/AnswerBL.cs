
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class AnswerBL
    {
        public long insertAnswer(Answer currentAnswer)
        {
            using (AnswerDA da = new AnswerDA())
            {
                return da.insertAnswer(currentAnswer);
            }
        }

        public List<Answer> getAllAnswer()
        {
            using (AnswerDA da = new AnswerDA())
            {
                return da.getAllAnswers();
            }
        }
    }
}