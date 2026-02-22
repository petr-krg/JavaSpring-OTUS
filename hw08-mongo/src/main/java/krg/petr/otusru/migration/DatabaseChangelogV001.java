package krg.petr.otusru.migration;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;


@ChangeLog(order = "001")
public class DatabaseChangelogV001 {

    @ChangeSet(order = "001", id = "dropDb", author = "petr.krg", runAlways = true)
    public void dropDb(MongoDatabase db) {
        db.drop();
    }

    @ChangeSet(order = "002", id = "insertInitialData", author = "petr.krg")
    public void insertInitialData(MongoDatabase db) {
        MongoCollection<Document> authors = db.getCollection("authors");
        authors.insertMany(List.of(
                new Document("_id", 1L).append("fullName", "Author_1"),
                new Document("_id", 2L).append("fullName", "Author_2"),
                new Document("_id", 3L).append("fullName", "Author_3")
        ));

        MongoCollection<Document> genres = db.getCollection("genres");
        genres.insertMany(List.of(
                new Document("_id", 1L).append("name", "Genre_1"),
                new Document("_id", 2L).append("name", "Genre_2"),
                new Document("_id", 3L).append("name", "Genre_3"),
                new Document("_id", 4L).append("name", "Genre_4"),
                new Document("_id", 5L).append("name", "Genre_5"),
                new Document("_id", 6L).append("name", "Genre_6")
        ));

        MongoCollection<Document> books = db.getCollection("books");
        books.insertMany(List.of(
                new Document("_id", 1L)
                        .append("title", "BookTitle_1")
                        .append("authorId", 1L)
                        .append("genreIds", List.of(1L, 2L)),
                new Document("_id", 2L)
                        .append("title", "BookTitle_2")
                        .append("authorId", 2L)
                        .append("genreIds", List.of(3L, 4L)),
                new Document("_id", 3L)
                        .append("title", "BookTitle_3")
                        .append("authorId", 3L)
                        .append("genreIds", List.of(5L, 6L))
        ));

        MongoCollection<Document> comments = db.getCollection("comments");
        comments.insertMany(List.of(
                new Document("_id", 1L).append("text", "Comment_1 for Book_1").append("bookId", 1L),
                new Document("_id", 2L).append("text", "Comment_2 for Book_1").append("bookId", 1L),
                new Document("_id", 3L).append("text", "Comment_1 for Book_2").append("bookId", 2L),
                new Document("_id", 4L).append("text", "Comment_1 for Book_3").append("bookId", 3L)
        ));
    }

    @ChangeSet(order = "003", id = "createIndexes", author = "petr.krg")
    public void createIndexes(MongoDatabase db) {
        db.getCollection("books").createIndex(new Document("authorId", 1));
        db.getCollection("comments").createIndex(new Document("bookId", 1));
    }

}