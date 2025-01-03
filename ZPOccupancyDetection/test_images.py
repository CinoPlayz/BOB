import os
import train_model_test
import image_processing

'''countOfRight = 0
countOfTotal = 0
for pathToFile in os.listdir('./test_images'):
    fullPath = os.path.join('./test_images', pathToFile)
    if os.path.isfile(fullPath):
        countOfPeople = image_processing.detect_people(fullPath, 0.75)
        
        indexLastDash = pathToFile.rfind('_')
        indexJpg = pathToFile.rfind('.jpg')
        numberString = pathToFile[indexLastDash+1:indexJpg]
        trueNumberOfPeople = int(numberString)
        
        print(pathToFile, countOfPeople, trueNumberOfPeople)

        if(countOfPeople == trueNumberOfPeople):
            countOfRight += 1
        
        countOfTotal += 1
        
print("Right guesses: ", countOfRight, "Wrong guesses: ", countOfTotal-countOfRight, " Accuracy: ", (countOfRight*100)/countOfTotal)
'''
countOfRight = 0
countOfTotal = 0
for pathToFile in os.listdir('./test_images'):
    fullPath = os.path.join('./test_images', pathToFile)
    if os.path.isfile(fullPath):
        countOfPeople = train_model_test.detect_people(fullPath, './fasterrcnn_model.pth', 0.75)
        
        indexLastDash = pathToFile.rfind('_')
        indexJpg = pathToFile.rfind('.jpg')
        numberString = pathToFile[indexLastDash+1:indexJpg]
        trueNumberOfPeople = int(numberString)
        
        print(pathToFile, countOfPeople, trueNumberOfPeople)

        if(countOfPeople == trueNumberOfPeople):
            countOfRight += 1
        
        countOfTotal += 1
        
print("Right guesses: ", countOfRight, "Wrong guesses: ", countOfTotal-countOfRight, " Accuracy: ", (countOfRight*100)/countOfTotal)

