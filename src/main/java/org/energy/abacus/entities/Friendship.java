package org.energy.abacus.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "findFriendshipByUsers", query = "SELECT f FROM Friendship f WHERE f.requestReceiverId = :receiver AND f.requestSenderId = :sender"),
        @NamedQuery(name = "updateFriendshipByUsers", query = "UPDATE Friendship SET accepted = :reaction WHERE requestReceiverId = :receiver AND requestSenderId = :sender"),
        @NamedQuery(name = "deleteFriendshipByUsers", query = "DELETE FROM Friendship WHERE requestReceiverId = :receiver AND requestSenderId = :sender"),
        @NamedQuery(name = "findFriendshipUsers", query = "SELECT f FROM Friendship f WHERE f.requestReceiverId = :receiver AND f.accepted = true")

})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String requestSenderId;

    @Column(nullable = false)
    private String requestReceiverId;


    boolean accepted;
}
