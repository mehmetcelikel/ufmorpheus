#!/usr/bin/python

def ngrambuilder(sentence, n=3):
	""" Returns ngram for the input list sentence of size n"""
	indexes = range(len(sentence))
	grams = []
	for start in range(len(sentence)):
		for end in filter(lambda x: x>start,range(1, min(len(sentence),n+start)+1)):
			grams.extend([sentence[start:end]])
	return grams


import sys
sys.path.append('../msrngram')
sys.path.append('./msrngram')
try:
	import msrngram
except:
	print 'cannot find msrngram'


def cond(phrases):
	return msrngram.GetConditionalProbability('2312312312',
				'urn:ngram:bing-title:jun09:4', phrases)


def prob(phrases):
	return msrngram.GetProbability('2312312312',
				'urn:ngram:bing-title:jun09:4', phrases)


if __name__ == '__main__' and __package__ is None:
	import argparse
	parser = argparse.ArgumentParser(description='''
				This is the scratch file to work with files needed for
				maverick. The --terms command builds ngrams ''', 
				add_help=True,
				epilog='contact cgrant@cise.ufl.edu for questions')
	parser.add_argument('--size', '-k', type=int, 
				help='max size of gram', required=False, default=3)
	parser.add_argument('--terms', '-t', help='Return k-grams',
				 nargs='+', required=True)
	args = parser.parse_args()
	
	if args.terms != None:
		print ngrambuilder(args.terms, args.size)
