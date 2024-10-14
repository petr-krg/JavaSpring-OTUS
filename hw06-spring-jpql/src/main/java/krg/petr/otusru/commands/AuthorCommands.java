package krg.petr.otusru.commands;

import krg.petr.otusru.converters.AuthorConverter;
import krg.petr.otusru.services.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
public class AuthorCommands {

    private final AuthorService authorService;

    private final AuthorConverter authorConverter;

    @ShellMethod(value = "Find all authors", key = "aa")
    public String findAllAuthors() {
        return authorService.findAll().stream()
                .map(authorConverter::authorToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Find author by id", key = "abid")
    public String findAuthorById(long id) {
        return authorService.findById(id)
                .map(authorConverter::authorToString)
                .orElse("Author with id %d not found!".formatted(id));
    }
}