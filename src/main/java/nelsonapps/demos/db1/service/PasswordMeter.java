package nelsonapps.demos.db1.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordMeter {

	private String password;
	private int totalScore = 0;
	private Map<Character,Integer>recurrency = new HashMap<>();
	private Map<Character,List<Integer>>recurrencyIndex = new HashMap<>();
	private boolean minimumCharacterLength=false;
	private boolean haveLowercaseLetters=false;
	private boolean haveUppercaseLetters=false;
	private boolean haveNumbers=false;
	private boolean haveSymbols=false;
	private int countConsecutive = 0;
	private int countSequence = 0;
	
	private Boolean[] minimumRequirements= new Boolean[]{haveLowercaseLetters,
			haveUppercaseLetters,haveNumbers,haveSymbols};
	
	public PasswordMeter(String password){
		this.password = password;
	}
	
	public int scorePassword(){
		totalScore +=ratePasswordLength();
		totalScore +=rateUpperCaseLetters();
		totalScore +=rateLowerCaseLetters();
		totalScore +=rateNumbers();
		totalScore +=rateSymbols();
		totalScore +=rateMiddleSymbolsOrNumbers();
		totalScore +=rateRequirements();
		totalScore-=rateOnlyNumberOrLetter();
		totalScore-=rateConsecutiveNumberOrLetters();
		totalScore-=rateSequencies();
		totalScore-=rateRepeatedChars();
		return totalScore;
	}

	private int ratePasswordLength() {
		minimumCharacterLength = password.length() == 8;
		return (password.length()*4);
	}
	
	private int rateUpperCaseLetters() {
		int ocurrences = 0;
		Pattern pattern = Pattern.compile("[A-Z]+");
		Matcher match = pattern.matcher(password);
		while(match.find()){
			if (match.group(0).length() > 1)
				countConsecutive += (match.group(0).length() - 1);
			
			haveUppercaseLetters = true;
			for(char letter : match.group(0).toCharArray()){
				checkSequence(Character.toLowerCase(letter),match.group(0));
				if(recurrency.containsKey(Character.toLowerCase(letter))){
					int count = recurrency.get(Character.toLowerCase(letter));
					recurrency.put(Character.toLowerCase(letter), ++count);
				} else {
					recurrency.put(Character.toLowerCase(letter), 1);
				}
			}
			ocurrences+=match.group(0).length();
		}
		
		return ocurrences==0 ? 0 : (password.length()-ocurrences)*2;
	}
	
	private int rateLowerCaseLetters() {
		int ocurrences = 0;
		Pattern pattern = Pattern.compile("[a-z]+");
		Matcher match = pattern.matcher(password);
		while(match.find()){
			if (match.group(0).length() > 1)
				countConsecutive += (match.group(0).length() - 1);
			haveLowercaseLetters = true;
			
			for(char letter : match.group(0).toCharArray()){
				checkSequence(letter,match.group(0));
				if(recurrency.containsKey(letter)){
					int count = recurrency.get(letter);
					recurrency.put(letter, ++count);
				} else {
					recurrency.put(letter, 1);
				}
			}
			
			ocurrences+=match.group(0).length();
		}
		
		return ocurrences==0 ? 0: (password.length()-ocurrences)*2;
	}
	
	private int rateNumbers() {
		int ocurrences = 0;
		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher match = pattern.matcher(password);
		while(match.find()){
			if (match.group(0).length() > 1)
				countConsecutive += (match.group(0).length() - 1);
			
			haveNumbers = true;
			for(char char_num : match.group(0).toCharArray()){
				checkSequence(char_num,match.group(0));
				if(recurrency.containsKey(char_num)){
					int count = recurrency.get(char_num);
					recurrency.put(char_num, ++count);
				} else {
					recurrency.put(char_num, 1);
				}
			}
			ocurrences+=match.group(0).length();
		}
		
		if(password.matches("[0-9]{"+password.length()+"}")){
			return 0;
		} else {
			return ocurrences*4;
		}
		
	}
	
	private int rateSymbols(){
		int ocurrences = 0;
		Pattern pattern = Pattern.compile("\\)+|\\!+|\\@+|\\#+|\\$+|\\%+|\\^+|\\&+|\\*+|\\(+");
		Matcher match = pattern.matcher(password);
		while(match.find()){
			haveSymbols = true;
			for(char char_symbol : match.group(0).toCharArray()){
				checkSequence(char_symbol,match.group(0));
				if(recurrency.containsKey(char_symbol)){
					int count = recurrency.get(char_symbol);
					recurrency.put(char_symbol, ++count);
				} else {
					recurrency.put(char_symbol, 1);
				}
			}
			
			ocurrences++;
		}
		
		return ocurrences == 0 ? 0 : ocurrences*6;
	}

	private int rateMiddleSymbolsOrNumbers(){
		int rate = 0;
		if (password.length() >= 3) {
			Pattern pattern = Pattern.compile(".([0-9]|\\)|\\!|\\@|\\#|\\$|\\%|\\^|\\&|\\*|\\()+.");
			Matcher match = pattern.matcher(password);
			if(match.find()) {
				if (match.group(1)!= null) {
					Character matchMiddle = match.group(1).charAt(0);
					if (recurrency.containsKey(matchMiddle)) {
						int count = recurrency.get(matchMiddle);
						recurrency.put(matchMiddle, ++count);
					} else {
						recurrency.put(matchMiddle, 1);
					}
					rate += 2;
				}
			}

		}

		return rate;
	}
	
	private int rateRequirements(){
		if(minimumCharacterLength && Arrays.asList(minimumRequirements)
				.stream().filter(condition -> condition==true).count()==3)
			return 8;
		else
			return 0;
	}
	
	private int rateOnlyNumberOrLetter(){
		int deduction = 0;
		int length = password.length();
		
		if(password.matches("[0-9]{"+length+"}") || password.matches("[a-zA-Z]{"+length+"}"))
			deduction+=length;
	
		return deduction;
	}
	
	private int rateConsecutiveNumberOrLetters() {
		return 2*(countConsecutive);
	}
	
	private void checkSequence(char char_symbol, String groupMatch) {
		String sequence="";
		String symbolSet = ")!@#$%^&*()";
		Character nextInSequence = null;
		char reference = Character.isLetter(char_symbol) ? Character.toLowerCase(char_symbol) : char_symbol;
		
		if (Character.isDigit(reference) || Character.isLetter(reference)) {
			for (int i = 1; i <= 2; i++) {
				if ((reference == '9' && i==1) || (nextInSequence != null && nextInSequence.charValue() == '9')) {
					nextInSequence = Character.valueOf('0');
				} else {
					nextInSequence = (nextInSequence == null) ? Character.valueOf((char) (reference + 1))
							: Character.valueOf((char) (nextInSequence.charValue() + 1));
				}
				
				sequence+=String.valueOf(nextInSequence.charValue());
			}
		}
		
	    String passwordLower = password.toLowerCase(Locale.ROOT);
	    if(passwordLower.contains(String.valueOf(char_symbol)+sequence)){
	    	countSequence++;
	    }
		
	    for(int i=0;i<symbolSet.length()-3;i++){
	    	String symbolSequence = symbolSet.substring(i, i+3);
	    	if(password.contains(symbolSequence))
	    		countSequence++;
	    }
		
	}
	
	private int rateSequencies(){
		return 3*countSequence;
	}
	
	private int rateRepeatedChars() {
		int deduction = 0;
		int passwordLen = password.length();
		int repeatedCharCount = 0;

		if (!recurrency.isEmpty()) {
			for (Entry<Character, Integer> pair : recurrency.entrySet()) {
				List<Integer> index = new ArrayList<>();
				int count = 0;
				repeatedCharCount += pair.getValue();
				for (int i = 0; i < passwordLen; i++) {
					if (password.charAt(i) == pair.getKey()) {
						count++;
						index.add(i);
					}
					if (count == pair.getValue())
						break;
				}

				recurrencyIndex.put(pair.getKey(), index);
			}

			for (Entry<Character, List<Integer>> pair : recurrencyIndex.entrySet()) {
				double recurrencyIncrement = 0;
				for (int i = 0; i < pair.getValue().size() - 1; i++) {
					recurrencyIncrement += Math
							.abs(passwordLen / (pair.getValue().get(i + 1) - pair.getValue().get(i)));

				}

				deduction += Math.ceil(recurrencyIncrement / (passwordLen - repeatedCharCount));

			}
		}
		return deduction;

	}
	
}
