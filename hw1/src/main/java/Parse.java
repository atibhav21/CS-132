import java.util.*;

class ParseException extends Exception {
	public ParseException(String s) {
		super(s);
	}	
}

enum TokenType {
	OPEN_BRACKET, CLOSE_BRACKET, PRINT_ST, OPEN_PARENTHESES, CLOSE_PARENTHESES, SEMICOLON, IF, ELSE, WHILE, TRUE, FALSE, NOT;
}

class Tokenizer {
	public Tokenizer() {
		string_to_token_type.put("{", TokenType.OPEN_BRACKET);
		string_to_token_type.put("}", TokenType.CLOSE_BRACKET);
		string_to_token_type.put("System.out.println", TokenType.PRINT_ST);
		string_to_token_type.put("(", TokenType.OPEN_PARENTHESES);
		string_to_token_type.put(")", TokenType.CLOSE_PARENTHESES);
		string_to_token_type.put(";", TokenType.SEMICOLON);
		string_to_token_type.put("if", TokenType.IF);
		string_to_token_type.put("else", TokenType.ELSE);
		string_to_token_type.put("while", TokenType.WHILE);
		string_to_token_type.put("true", TokenType.TRUE);
		string_to_token_type.put("false", TokenType.FALSE);
		string_to_token_type.put("!", TokenType.NOT);
	}

	public ArrayList<TokenType> ConvertToTokens(String s) throws ParseException {
		String current_buffer = "";
		ArrayList<TokenType> tokens_in_string = new ArrayList<TokenType>();
		for(int i = 0; i < s.length(); i++) {
			current_buffer += s.charAt(i);
			if (string_to_token_type.keySet().contains(current_buffer)) {
				tokens_in_string.add(string_to_token_type.get(current_buffer));
				current_buffer = "";
			}
		}
		if (!current_buffer.isEmpty()) {
			throw new ParseException("Parse error");
		}
		return tokens_in_string;
	}

	private HashMap<String, TokenType> string_to_token_type = new HashMap<String, TokenType>();
}

class Parser {
	public void addTokens(ArrayList<TokenType> tokens_to_add) {
		tokens.addAll(tokens_to_add);
	}

	public void printTokens() {
		for(TokenType token : tokens) {
			System.out.println(token);
		}
	}

	private void eatToken(TokenType token) throws ParseException{
		try {
			if(tokens.get(counter) == token) {
				counter += 1;
			}
			else {
				throw new ParseException("Parse Error");
			}
		}
		catch(IndexOutOfBoundsException e) {
			throw new ParseException("Parse Error");
		}
	}

	private void parseS() throws ParseException{
		if(tokens.get(counter) == TokenType.OPEN_BRACKET) {
			eatToken(TokenType.OPEN_BRACKET);
			parseL();
			eatToken(TokenType.CLOSE_BRACKET);
		}
		else if (tokens.get(counter) == TokenType.PRINT_ST) {
			eatToken(TokenType.PRINT_ST);
			eatToken(TokenType.OPEN_PARENTHESES);
			parseE();
			eatToken(TokenType.CLOSE_PARENTHESES);
			eatToken(TokenType.SEMICOLON);
		}
		else if (tokens.get(counter) == TokenType.IF) {
			eatToken(TokenType.IF);
			eatToken(TokenType.OPEN_PARENTHESES);
			parseE();
			eatToken(TokenType.CLOSE_PARENTHESES);
			parseS();
			eatToken(TokenType.ELSE);
			parseS();
		}
		else if (tokens.get(counter) == TokenType.WHILE) {
			eatToken(TokenType.WHILE);
			eatToken(TokenType.OPEN_PARENTHESES);
			parseE();
			eatToken(TokenType.CLOSE_PARENTHESES);
			parseS();
		}
		else {
			throw new ParseException("Parse error");
		}
	}

	private void parseL() throws ParseException{
		if (tokens.get(counter) == TokenType.OPEN_BRACKET || 
			tokens.get(counter) == TokenType.PRINT_ST || 
			tokens.get(counter) == TokenType.IF || 
			tokens.get(counter) == TokenType.WHILE) {
			parseS();
			parseL();
		}
		else {
			// epsilon
		}
	}

	private void parseE() throws ParseException{
		if (tokens.get(counter) == TokenType.TRUE) {
			eatToken(TokenType.TRUE);
		}
		else if(tokens.get(counter) == TokenType.FALSE) {
			eatToken(TokenType.FALSE);
		}
		else if(tokens.get(counter) == TokenType.NOT) {
			eatToken(TokenType.NOT);
			parseE();
		}
		else {
			throw new ParseException("Parse error");
		}
	}

	public void parse() throws ParseException {
		parseS();
		if(counter != tokens.size()) {
			throw new ParseException("Parse error");
		}
	}

	private ArrayList<TokenType> tokens = new ArrayList<TokenType>();
	private int counter = 0;
}

public class Parse {
	public static void main(String [] args) {
		Tokenizer tokenizer = new Tokenizer();
		Parser parser = new Parser();

		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNext()) {
			try {
				parser.addTokens(tokenizer.ConvertToTokens(scanner.next()));
			}
			catch (ParseException e) {
				System.out.println("Parse error");
				System.exit(1);
			}
		}
		try {
			parser.parse();
		}
		catch (ParseException e) {
			System.out.println("Parse error");
			System.exit(1);
		}
		System.out.println("Program parsed successfully");
		scanner.close();
	}

}
