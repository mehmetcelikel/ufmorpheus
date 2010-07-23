'''
Created on May 30, 2010

@author: Joir-dan
'''
import nltk
import nltk.corpus

whatList = ["what", "What"]
tagCount = {}
tagSentenceIsQuestion = []
nextGram = {"isVerb" : 0, "isNoun": 0, "isOther" : 0}
isQuestion = {"yes":0, "no": 0}
brownText = nltk.corpus.brown;
file = open("/Users/Hikari/Desktop/brown-corpus.info", "a")
file.write("Analysis of brown corpus: sentences with [what] in them\n")
for sentence in brownText.tagged_sents():
    finish = 0
    for i in range(len(sentence) -1):
        j = i+1;
        word, tag = sentence[i]
        word2, tag2 = sentence[j]
        if word in whatList:
            if tagCount.has_key(tag): tagCount[tag] += 1
            else: tagCount[tag] = 1   
            if tag2[0] is 'V': nextGram["isVerb"] += 1
            elif tag2[0] is 'N': nextGram["isNoun"] += 1
            else: nextGram["isOther"] += 1
            finish = 1;
            break
    if finish == 1:
        lastTerm = sentence[len(sentence)-1]
        lastWord, lastTag = lastTerm
        q = "yes" if lastWord is '?' else "no"
        isQuestion[q]+= 1
        untagged = nltk.tag.untag(sentence);
        tagSentenceIsQuestion.append((tag, untagged, q))
    
for tag, count in tagCount.items():
    file.write("Tag: "+ tag+ ", Count: "+ str(count)+ "\n")
    for sentTag, sent, isQuery in tagSentenceIsQuestion:
        if(sentTag == tag):
            if(isQuery is "yes"):
                file.write(sentTag+":"+isQuery+":"+" ".join(sent)+"\n")
                
file.write("Bigram Information... frequency of tagType for w2\n")
for gramType, count in nextGram.items():
    file.write(gramType+": "+str(count)+"\n")
file.close()
    