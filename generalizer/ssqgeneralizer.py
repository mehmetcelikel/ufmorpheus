#!/usr/bin/python


def gen(tcpairs):
	""" Takes a list of term class pairs and outputs the minimum # terms 

	It takes in a list of term class pairs.
	Returns a list of the smallest (non overlaping terms) and another list with
	the index of each term and possible classes

	>>> gen((('Toyota Camry V6', 'Sedan'),('1997','Year'),('Toyota','Manufacturer'),('V6','Engine'),('Camry V6','Model')))
	([['Toyota', 'Camry', 'V6'], ['1997']], [[['Toyota', 'Camry', 'V6'], ['0:0:3'], ['Sedan']], [['Camry', 'V6'], ['0:1:2'], ['Model']], [['1997'], ['0:0:1'], ['Year']], [['Toyota'], ['0:0:1'], ['Manufacturer']], [['V6'], ['0:2:1'], ['Engine']]])

	"""
	cache = [] # Has the shortest amount of phrases, this will be inputs
	index = [] # Indexes pointers maps of classes to cache phrase elements
	query_terms = sorted([a for a in [e[0].split() for e in tcpairs]], \
				key=len, reverse=True) # Sort Most words to the least 
	for phrase in query_terms:
		if exists(phrase,cache):
			# Add class and pointer to the index
			index += [[phrase, [get_cache_location(phrase, cache)],
				lookup_class(tcpairs,phrase)]]
		else:
			# Add to cache and class and index pointer
			cache += [phrase]
			index += [[phrase, [get_cache_location(phrase, cache)],
				lookup_class(tcpairs,phrase)]]

	# Return class and index pointer or cache??
	return (cache, index)


def lookup_class(tcpairs, phrase):
	""" Return the class for the particular phrase """
	tcps =  map(list, tcpairs)
	phs = ' '.join(phrase)
	z = [c for (t,c) in tcps if t==phs]	
	return "UNKNOWN" if not z else z


def get_cache_location(term, cache):
	""" Returns the index key for the location of the term in the cache.

			The key looks like %d:%d:%d. The first number is the cach index, which
			is zero indexed.  The second number is the begining word index, the 
			third number is the phrase length. [start, end)
			
			This method assumes all words in terms are unique.
			This assumes order of term is consistent to that order in the cache.
	"""
	index = 0
	start = 0
	length = 0
	for phrase in cache:
		if term == phrase:
			return '%d:%d:%d'%(0,0,len(term))
		if exists(term, phrase):
			start_found = False
			for word in phrase:
				if word == term[0]:
					start_found = True
					length+=1
				if word != term[-1] and start_found:
					length+=1
				if word == term[-1]:
					break
				if not start_found:
					start+=1
			break
		index +=1

	return '%d:%d:%d'%(index,start,length)


def exists(phrase, cache):
	""" Returns true if term is in the cache """
	z = []
	for p in phrase:
		z += [any([p in c for c in cache] + [False])]
	return all(z)


def ngrambuilder(sentence, n=3):
  """ Returns ngram for the input list sentence of size n"""
  indexes = range(len(sentence))
  grams = []
  for start in range(len(sentence)):
    for end in filter(lambda x: x>start,range(1, min(len(sentence),n+start)+1)):
      grams.extend([sentence[start:end]])
  return grams	


if __name__ == '__main__':
	import argparse
	parser = argparse.ArgumentParser(description='''Takes ssq and builds inputs
					for it.  ...Among other things. ''', add_help=True)
	import doctest
	doctest.testmod()
	#parser.add_argument('--example',
	#print 'example: %s' % (str((('Toyota Camry V6', 'Sedan'), ('1997', 'Year'),\
	#			('Toyota', 'Manufacturer'), ('V6', 'Engine'), ('Camry V6', 'Model'))))
	#tcpairs = (('Toyota Camry V6', 'Sedan'), ('1997', 'Year'),\
	#			('Toyota', 'Manufacturer'), ('V6', 'Engine'), ('Camry V6', 'Model'))
	#print
	#print '>>>',gen(tcpairs) 
