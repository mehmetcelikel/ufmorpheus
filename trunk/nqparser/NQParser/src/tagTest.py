'''
Created on Jun 4, 2010

@author: Joir-dan
'''
import nltk
from nltk.tagger.brill import *
    


print("Training tagger")
backoffTagger = nltk.data.load(nltk.tag._POS_TAGGER)
uniTagger = nltk.tag.UnigramTagger(nltk.corpus.treebank.tagged_sents()[:2000], backoff = backoffTagger)
print("Done Training...")

choice = "";
while choice != "2":
    print("1: Run test \n 2: Exit \n")
    choice = raw_input("Choice: ")
    if choice == "1":
        text = raw_input("Question: ")
        terms = nltk.tokenize.word_tokenize(text)
        taggedTerms = uniTagger.tag(terms)
        print(taggedTerms)